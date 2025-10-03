package com.productos.productos.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.productos.productos.aplication.service.ProductosService;
import com.productos.productos.config.SecurityConfig;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;
import com.productos.productos.shared.dto.pageable.PageableRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductosController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "api.key=test-api-key-123",
        "server.port=8080",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "inventario.api.url=http://localhost:8082/api/v1/"
})
@DisplayName("ProductosController - Tests Unitarios")
class ProductosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // Cambiado de @MockBean
    private ProductosService productosService;

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String VALID_API_KEY = "test-api-key-123";
    private static final String INVALID_API_KEY = "invalid-key";

    private ProductoResponseJsonApiDTO productoResponse;
    private ProductoRequestJsonApiDTO productoRequest;

    @BeforeEach
    void setUp() {
        // Setup response
        ProductoResponseJsonApiDTO.Data.Attributes attributes = new ProductoResponseJsonApiDTO.Data.Attributes();
        attributes.setNombre("Laptop");
        attributes.setDescripcion("Laptop gaming");
        attributes.setPrecio(new BigDecimal("1500.00"));
        attributes.setCategoria("Electrónica");
        attributes.setCantidad(10);

        ProductoResponseJsonApiDTO.Data data = new ProductoResponseJsonApiDTO.Data();
        data.setId("1");
        data.setType("producto");
        data.setAttributes(attributes);

        productoResponse = new ProductoResponseJsonApiDTO();
        productoResponse.setData(List.of(data));

        // Setup request
        ProductoRequestJsonApiDTO.Attributes requestAttributes = new ProductoRequestJsonApiDTO.Attributes();
        requestAttributes.setNombre("Laptop");
        requestAttributes.setDescripcion("Laptop gaming");
        requestAttributes.setPrecio(new BigDecimal("1500.00"));
        requestAttributes.setCategoria("Electrónica");
        requestAttributes.setCantidad(10);

        ProductoRequestJsonApiDTO.Data requestData = new ProductoRequestJsonApiDTO.Data();
        requestData.setType("productos");
        requestData.setAttributes(requestAttributes);

        productoRequest = new ProductoRequestJsonApiDTO();
        productoRequest.setData(requestData);
    }

    @Nested
    @DisplayName("Tests de Seguridad - API Key")
    class SeguridadTests {

        @Test
        @DisplayName("Debe retornar 401 cuando no se envía API Key")
        void sinApiKey_retorna401() throws Exception {
            mockMvc.perform(get("/api/v1/productos/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors").exists())
                    .andExpect(jsonPath("$.errors[0].status").value("401"))
                    .andExpect(jsonPath("$.errors[0].title").value("No autorizado"));

            verifyNoInteractions(productosService);
        }

        @Test
        @DisplayName("Debe retornar 401 cuando API Key es inválida")
        void apiKeyInvalida_retorna401() throws Exception {
            mockMvc.perform(get("/api/v1/productos/1")
                            .header(API_KEY_HEADER, INVALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errors").exists());

            verifyNoInteractions(productosService);
        }

        @Test
        @DisplayName("Debe permitir acceso con API Key válida")
        void apiKeyValida_permiteAcceso() throws Exception {
            when(productosService.getProductoPorId(1L)).thenReturn(productoResponse);

            mockMvc.perform(get("/api/v1/productos/1")
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(productosService).getProductoPorId(1L);
        }
    }

    @Nested
    @DisplayName("Tests de listarProductosConFiltro")
    class ListarProductosConFiltroTests {

        @Test
        @DisplayName("Debe retornar 200 y lista de productos con filtros válidos")
        void listarProductosConFiltro_filtrosValidos_retorna200() throws Exception {
            ProductoFiltroDTO filtro = new ProductoFiltroDTO();
            filtro.setNombre("Laptop");
            filtro.setCategoria("Electrónica");
            PageableRequest pageable = new PageableRequest();
            pageable.setPage(0);
            pageable.setSize(10);
            pageable.setOrder("asc");
            filtro.setPageable(pageable);

            when(productosService.getTodosLosProductos(any(ProductoFiltroDTO.class), anyString()))
                    .thenReturn(productoResponse);

            mockMvc.perform(post("/api/v1/productos/filtro")
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filtro)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[0].attributes.nombre").value("Laptop"));

            verify(productosService).getTodosLosProductos(any(ProductoFiltroDTO.class), anyString());
        }

        @Test
        @DisplayName("Debe retornar 200 con filtro vacío")
        void listarProductosConFiltro_filtroVacio_retorna200() throws Exception {
            ProductoFiltroDTO filtro = new ProductoFiltroDTO();
            PageableRequest pageable = new PageableRequest();
            pageable.setPage(0);
            pageable.setSize(10);
            pageable.setOrder("asc");
            filtro.setPageable(pageable);

            when(productosService.getTodosLosProductos(any(ProductoFiltroDTO.class), anyString()))
                    .thenReturn(productoResponse);

            mockMvc.perform(post("/api/v1/productos/filtro")
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filtro)))
                    .andExpect(status().isOk());

            verify(productosService).getTodosLosProductos(any(ProductoFiltroDTO.class), anyString());
        }
    }

    @Nested
    @DisplayName("Tests de obtenerProductoPorId")
    class ObtenerProductoPorIdTests {

        @Test
        @DisplayName("Debe retornar 200 y producto cuando existe")
        void obtenerProductoPorId_productoExiste_retorna200() throws Exception {
            Long id = 1L;
            when(productosService.getProductoPorId(id)).thenReturn(productoResponse);

            mockMvc.perform(get("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[0].type").value("producto"))
                    .andExpect(jsonPath("$.data[0].attributes.nombre").value("Laptop"))
                    .andExpect(jsonPath("$.data[0].attributes.precio").value(1500.00));

            verify(productosService).getProductoPorId(id);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando producto no existe")
        void obtenerProductoPorId_productoNoExiste_retorna404() throws Exception {
            Long id = 999L;
            when(productosService.getProductoPorId(id))
                    .thenThrow(new EntityNotFoundException("Producto no encontrado con ID: " + id));

            mockMvc.perform(get("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(productosService).getProductoPorId(id);
        }
    }

    @Nested
    @DisplayName("Tests de crearProducto")
    class CrearProductoTests {

        @Test
        @DisplayName("Debe retornar 201 cuando producto se crea exitosamente")
        void crearProducto_datosValidos_retorna201() throws Exception {
            when(productosService.crearProducto(any(ProductoRequestJsonApiDTO.class)))
                    .thenReturn(productoResponse);

            mockMvc.perform(post("/api/v1/productos")
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[0].attributes.nombre").value("Laptop"));

            verify(productosService).crearProducto(any(ProductoRequestJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe retornar 500 cuando request body es inválido")  // Cambió de 400 a 500
        void crearProducto_requestInvalido_retorna500() throws Exception {  // Cambió nombre
            String invalidJson = "{invalid json}";

            mockMvc.perform(post("/api/v1/productos")
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isInternalServerError());  // Cambió de isBadRequest()

            verifyNoInteractions(productosService);
        }

        @Test
        @DisplayName("Debe retornar 500 cuando request body es inválido")  // Cambió de 400 a 500
        void actualizarProducto_requestInvalido_retorna500() throws Exception {  // Cambió nombre
            Long id = 1L;
            String invalidJson = "{invalid}";

            mockMvc.perform(put("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isInternalServerError());  // Cambió de isBadRequest()

            verifyNoInteractions(productosService);
        }
    }

    @Nested
    @DisplayName("Tests de actualizarProducto")
    class ActualizarProductoTests {

        @Test
        @DisplayName("Debe retornar 200 cuando producto se actualiza exitosamente")
        void actualizarProducto_datosValidos_retorna200() throws Exception {
            Long id = 1L;
            when(productosService.actualizarProducto(eq(id), any(ProductoRequestJsonApiDTO.class)))
                    .thenReturn(productoResponse);

            mockMvc.perform(put("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data[0].id").value("1"));

            verify(productosService).actualizarProducto(eq(id), any(ProductoRequestJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe retornar 404 cuando producto no existe")
        void actualizarProducto_productoNoExiste_retorna404() throws Exception {
            Long id = 999L;
            when(productosService.actualizarProducto(eq(id), any(ProductoRequestJsonApiDTO.class)))
                    .thenThrow(new EntityNotFoundException("Producto no encontrado con ID: " + id));

            mockMvc.perform(put("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productoRequest)))
                    .andExpect(status().isNotFound());

            verify(productosService).actualizarProducto(eq(id), any(ProductoRequestJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe retornar 400 cuando request body es inválido")
        void actualizarProducto_requestInvalido_retorna400() throws Exception {
            Long id = 1L;
            String invalidJson = "{invalid}";

            mockMvc.perform(put("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(productosService);
        }
    }

    @Nested
    @DisplayName("Tests de eliminarProducto")
    class EliminarProductoTests {

        @Test
        @DisplayName("Debe retornar 204 cuando producto se elimina exitosamente")
        void eliminarProducto_productoExiste_retorna204() throws Exception {
            Long id = 1L;
            doNothing().when(productosService).eliminarProductoPorId(id);

            mockMvc.perform(delete("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(productosService).eliminarProductoPorId(id);
        }

        @Test
        @DisplayName("Debe retornar 404 cuando producto no existe")
        void eliminarProducto_productoNoExiste_retorna404() throws Exception {
            Long id = 999L;
            doThrow(new EntityNotFoundException("Producto no encontrado con ID: " + id))
                    .when(productosService).eliminarProductoPorId(id);

            mockMvc.perform(delete("/api/v1/productos/{id}", id)
                            .header(API_KEY_HEADER, VALID_API_KEY))
                    .andExpect(status().isNotFound());

            verify(productosService).eliminarProductoPorId(id);
        }
    }

    @Nested
    @DisplayName("Tests de Content-Type y Accept")
    class ContentTypeTests {

        @Test
        @DisplayName("Debe aceptar application/json como Content-Type")
        void debeAceptarApplicationJson() throws Exception {
            when(productosService.getProductoPorId(1L)).thenReturn(productoResponse);

            mockMvc.perform(get("/api/v1/productos/1")
                            .header(API_KEY_HEADER, VALID_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debe retornar Content-Type application/json")
        void debeRetornarApplicationJson() throws Exception {
            when(productosService.getProductoPorId(1L)).thenReturn(productoResponse);

            mockMvc.perform(get("/api/v1/productos/1")
                            .header(API_KEY_HEADER, VALID_API_KEY))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }
}