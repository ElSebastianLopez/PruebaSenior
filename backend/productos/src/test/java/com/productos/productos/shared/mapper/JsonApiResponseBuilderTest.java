package com.productos.productos.shared.mapper;

import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.pageable.PaginacionMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonApiResponseBuilder - Tests Unitarios")
class JsonApiResponseBuilderTest {
    private static final String BASE_URL = "http://localhost:8080/api/v1/productos";
    private List<ProductoResponseJsonApiDTO.Data> dataList;

    @BeforeEach
    void setUp() {
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

        dataList = List.of(data);
    }

    @Nested
    @DisplayName("Tests del Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar UnsupportedOperationException al intentar instanciar")
        void constructor_intentarInstanciar_lanzaExcepcion() throws Exception {
            var constructor = JsonApiResponseBuilder.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            InvocationTargetException exception = assertThrows(
                    InvocationTargetException.class,
                    () -> constructor.newInstance()
            );

            assertTrue(exception.getCause() instanceof UnsupportedOperationException);
            assertEquals("Utility class", exception.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("Tests de build - Primera Página")
    class PrimeraPaginaTests {

        @Test
        @DisplayName("Debe construir response correctamente para primera página")
        void build_primeraPagina_construyeCorrectamente() {
            Page<String> page = new PageImpl<>(
                    List.of("item1", "item2"),
                    PageRequest.of(0, 10),
                    25
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            assertNotNull(response);
            assertNotNull(response.getMeta());
            assertTrue(response.getMeta() instanceof PaginacionMeta);

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals(0, meta.getPage());
            assertEquals(10, meta.getSize());
            assertEquals(25, meta.getTotal());
            assertEquals(3, meta.getTotalPages());
            assertEquals("asc", meta.getOrder());

            assertNotNull(response.getData());
            assertEquals(1, response.getData().size());
            assertEquals("1", response.getData().get(0).getId());

            assertNotNull(response.getLinks());
            assertEquals(BASE_URL + "?page=0", response.getLinks().getSelf());
            assertEquals(BASE_URL + "?page=1", response.getLinks().getNext());
            assertEquals(BASE_URL + "?page=2", response.getLinks().getLast());
        }

        @Test
        @DisplayName("Debe configurar orden descendente correctamente")
        void build_primeraPaginaOrdenDesc_configuraOrdenCorrectamente() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(0, 10),
                    15
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "desc"
            );

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals("desc", meta.getOrder());
        }
    }

    @Nested
    @DisplayName("Tests de build - Última Página")
    class UltimaPaginaTests {

        @Test
        @DisplayName("Debe construir response sin link next para última página")
        void build_ultimaPagina_nextEsNull() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(2, 10),
                    25
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            assertNotNull(response.getLinks());
            assertEquals(BASE_URL + "?page=2", response.getLinks().getSelf());
            assertNull(response.getLinks().getNext());
            assertEquals(BASE_URL + "?page=2", response.getLinks().getLast());
        }

        @Test
        @DisplayName("Debe manejar correctamente página única")
        void build_paginaUnica_nextEsNull() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(0, 10),
                    5
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals(0, meta.getPage());
            assertEquals(1, meta.getTotalPages());
            assertEquals(BASE_URL + "?page=0", response.getLinks().getSelf());
            assertNull(response.getLinks().getNext());
            assertEquals(BASE_URL + "?page=0", response.getLinks().getLast());
        }
    }

    @Nested
    @DisplayName("Tests de build - Página Intermedia")
    class PaginaIntermediaTests {

        @Test
        @DisplayName("Debe construir response correctamente para página intermedia")
        void build_paginaIntermedia_construyeCorrectamente() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(1, 10),
                    25
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals(1, meta.getPage());
            assertEquals(3, meta.getTotalPages());
            assertEquals(BASE_URL + "?page=1", response.getLinks().getSelf());
            assertEquals(BASE_URL + "?page=2", response.getLinks().getNext());
            assertEquals(BASE_URL + "?page=2", response.getLinks().getLast());
        }
    }

    @Nested
    @DisplayName("Tests de build - Casos Especiales")
    class CasosEspecialesTests {

        @Test
        @DisplayName("Debe manejar lista de datos vacía")
        void build_listaVacia_construyeCorrectamente() {
            Page<String> page = new PageImpl<>(
                    Collections.emptyList(),
                    PageRequest.of(0, 10),
                    0
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    Collections.emptyList(),
                    BASE_URL,
                    "asc"
            );

            assertNotNull(response);
            assertNotNull(response.getData());
            assertTrue(response.getData().isEmpty());

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals(0, meta.getTotal());
            assertEquals(0, meta.getTotalPages());
        }

        @Test
        @DisplayName("Debe manejar múltiples elementos en data")
        void build_multiplesDatos_construyeCorrectamente() {
            ProductoResponseJsonApiDTO.Data data2 = new ProductoResponseJsonApiDTO.Data();
            data2.setId("2");
            data2.setType("producto");

            List<ProductoResponseJsonApiDTO.Data> multipleData = List.of(
                    dataList.get(0),
                    data2
            );

            Page<String> page = new PageImpl<>(
                    List.of("item1", "item2"),
                    PageRequest.of(0, 10),
                    2
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    multipleData,
                    BASE_URL,
                    "asc"
            );

            assertEquals(2, response.getData().size());
            assertEquals("1", response.getData().get(0).getId());
            assertEquals("2", response.getData().get(1).getId());
        }

        @Test
        @DisplayName("Debe construir links correctamente con diferentes baseUrl")
        void build_diferentesBaseUrl_construyeLinksCorrectamente() {
            String customBaseUrl = "https://api.ejemplo.com/productos";
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(1, 10),
                    25
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    customBaseUrl,
                    "asc"
            );

            assertTrue(response.getLinks().getSelf().startsWith(customBaseUrl));
            assertTrue(response.getLinks().getNext().startsWith(customBaseUrl));
            assertTrue(response.getLinks().getLast().startsWith(customBaseUrl));
        }

        @Test
        @DisplayName("Debe manejar diferentes tamaños de página")
        void build_diferentesTamanoPagina_construyeCorrectamente() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(0, 5),
                    25
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals(5, meta.getSize());
            assertEquals(5, meta.getTotalPages());
        }

        @Test
        @DisplayName("Debe calcular totalPages correctamente con división no exacta")
        void build_divisionNoExacta_calculaTotalPagesCorrectamente() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(0, 10),
                    23
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertEquals(23, meta.getTotal());
            assertEquals(3, meta.getTotalPages());
        }
    }

    @Nested
    @DisplayName("Tests de Validación de Estructura Completa")
    class ValidacionEstructuraTests {

        @Test
        @DisplayName("Debe tener todos los componentes del response inicializados")
        void build_responseCompleto_todosLosComponentesPresentes() {
            Page<String> page = new PageImpl<>(
                    List.of("item"),
                    PageRequest.of(0, 10),
                    15
            );

            ProductoResponseJsonApiDTO response = JsonApiResponseBuilder.build(
                    page,
                    dataList,
                    BASE_URL,
                    "asc"
            );

            assertNotNull(response, "Response no debe ser null");
            assertNotNull(response.getData(), "Data no debe ser null");
            assertNotNull(response.getMeta(), "Meta no debe ser null");
            assertNotNull(response.getLinks(), "Links no debe ser null");

            PaginacionMeta meta = (PaginacionMeta) response.getMeta();
            assertNotNull(meta.getPage(), "Page en meta no debe ser null");
            assertNotNull(meta.getSize(), "Size en meta no debe ser null");
            assertNotNull(meta.getTotal(), "Total en meta no debe ser null");
            assertNotNull(meta.getTotalPages(), "TotalPages en meta no debe ser null");
            assertNotNull(meta.getOrder(), "Order en meta no debe ser null");

            assertNotNull(response.getLinks().getSelf(), "Self link no debe ser null");
            assertNotNull(response.getLinks().getLast(), "Last link no debe ser null");
        }
    }
}