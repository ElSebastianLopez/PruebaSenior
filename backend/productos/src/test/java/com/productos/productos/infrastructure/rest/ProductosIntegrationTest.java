package com.productos.productos.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.productos.productos.domain.model.Producto;
import com.productos.productos.infrastructure.client.InventarioClient;
import com.productos.productos.infrastructure.persistence.SpringDataJpaRepository;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Productos API - Tests de Integración E2E")
class ProductosIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public InventarioClient inventarioClient() {
            return Mockito.mock(InventarioClient.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataJpaRepository springDataJpaRepository;

    @Autowired
    private InventarioClient inventarioClient;

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY = "test-api-key-123";
    private static final String BASE_URL = "/api/v1/productos";

    private ProductoRequestJsonApiDTO crearProductoRequest;

    @BeforeEach
    void setUp() {
        ProductoRequestJsonApiDTO.Attributes attributes = new ProductoRequestJsonApiDTO.Attributes();
        attributes.setNombre("Laptop Dell");
        attributes.setDescripcion("Laptop para desarrollo");
        attributes.setPrecio(new BigDecimal("2500.00"));
        attributes.setCategoria("Tecnología");
        attributes.setCantidad(15);

        ProductoRequestJsonApiDTO.Data data = new ProductoRequestJsonApiDTO.Data();
        data.setType("productos");
        data.setAttributes(attributes);

        crearProductoRequest = new ProductoRequestJsonApiDTO();
        crearProductoRequest.setData(data);

        InventarioResponseJsonApiDTO.Data.Attributes invAttributes = new InventarioResponseJsonApiDTO.Data.Attributes();
        invAttributes.setProductoId(1L);
        invAttributes.setCantidadDisponible(15);
        invAttributes.setUltimaActualizacion(LocalDateTime.now());

        InventarioResponseJsonApiDTO.Data invData = new InventarioResponseJsonApiDTO.Data();
        invData.setId("1");
        invData.setType("inventario");
        invData.setAttributes(invAttributes);

        Mockito.reset(inventarioClient);
        when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                .thenReturn(List.of(invData));
    }

    @AfterEach
    void cleanup() {
        springDataJpaRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Crear producto sin API Key debe retornar 401")
    void crearProductoSinApiKey_retorna401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearProductoRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors[0].status").value("401"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Listar productos con filtros y paginación")
    void listarProductosConFiltros_e2e() throws Exception {
        crearProductoEnBD("Mouse Logitech", "Tecnología", new BigDecimal("50.00"));
        crearProductoEnBD("Teclado Mecánico", "Tecnología", new BigDecimal("120.00"));
        crearProductoEnBD("Silla Gamer", "Muebles", new BigDecimal("450.00"));

        mockMvc.perform(post(BASE_URL + "/filtro")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageable\":{\"page\":0,\"size\":10,\"order\":\"asc\"}}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.meta.total", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.links.self").exists());

        mockMvc.perform(post(BASE_URL + "/filtro")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria\":\"Tecnología\",\"pageable\":{\"page\":0,\"size\":10,\"order\":\"asc\"}}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Actualizar producto inexistente debe retornar 404")
    void actualizarProductoInexistente_retorna404() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", 99999L)
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearProductoRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Recurso no encontrado"));
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Eliminar producto inexistente debe retornar 404")
    void eliminarProductoInexistente_retorna404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 99999L)
                        .header(API_KEY_HEADER, API_KEY))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private void crearProductoEnBD(String nombre, String categoria, BigDecimal precio) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setDescripcion("Descripción de " + nombre);
        springDataJpaRepository.save(producto);
    }
}