package com.productos.productos.infrastructure.client;

import static org.junit.jupiter.api.Assertions.*;

import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.InventarioListResponseJsonApiDTO;
import com.productos.productos.shared.dto.InventarioRequestJsonApiDTO;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventarioClient - Tests Unitarios")
class InventarioClientTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventarioClient inventarioClient;

    private static final String API_URL = "http://localhost:8082/api/v1/";
    private static final String API_KEY = "test-api-key";

    private InventarioResponseJsonApiDTO inventarioResponseValido;
    private InventarioListResponseJsonApiDTO inventarioListResponse;

    @BeforeEach
    void setUp() {
        // Configurar valores de @Value usando ReflectionTestUtils
        ReflectionTestUtils.setField(inventarioClient, "apiUrlInventario", API_URL);
        ReflectionTestUtils.setField(inventarioClient, "apiKey", API_KEY);

        // Response válido para crear inventario
        InventarioResponseJsonApiDTO.Data data = new InventarioResponseJsonApiDTO.Data();
        data.setId("1");
        data.setType("inventarios");

        InventarioResponseJsonApiDTO.Data.Attributes attributes = new InventarioResponseJsonApiDTO.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(10);
        data.setAttributes(attributes);

        inventarioResponseValido = new InventarioResponseJsonApiDTO();
        inventarioResponseValido.setData(data);

        // Response para listar inventarios
        InventarioResponseJsonApiDTO.Data dataList = new InventarioResponseJsonApiDTO.Data();
        dataList.setId("1");
        dataList.setType("inventarios");
        dataList.setAttributes(attributes);

        inventarioListResponse = new InventarioListResponseJsonApiDTO();
        inventarioListResponse.setData(List.of(dataList));
    }

    @Nested
    @DisplayName("Tests de crearInventarioConReintentos - Casos exitosos")
    class CrearInventarioExitososTests {

        @Test
        @DisplayName("Debe crear inventario exitosamente en el primer intento")
        void crearInventarioConReintentos_exitoPrimerIntento_retornaInventario() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            ResponseEntity<InventarioResponseJsonApiDTO> response =
                    new ResponseEntity<>(inventarioResponseValido, HttpStatus.CREATED);

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenReturn(response);

            // When
            InventarioResponseJsonApiDTO resultado = inventarioClient.crearInventarioConReintentos(productoId, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getData()).isNotNull();
            assertThat(resultado.getData().getId()).isEqualTo("1");
            verify(restTemplate, times(1)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe reintentar y tener éxito en el segundo intento")
        void crearInventarioConReintentos_exitoSegundoIntento_retornaInventario() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            ResponseEntity<InventarioResponseJsonApiDTO> responseExitoso =
                    new ResponseEntity<>(inventarioResponseValido, HttpStatus.CREATED);

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenThrow(HttpServerErrorException.create(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error temporal",
                            null,
                            "Temporary error".getBytes(),
                            null
                    ))
                    .thenReturn(responseExitoso);

            // When
            InventarioResponseJsonApiDTO resultado = inventarioClient.crearInventarioConReintentos(productoId, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            verify(restTemplate, times(2)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe reintentar y tener éxito en el tercer intento")
        void crearInventarioConReintentos_exitoTercerIntento_retornaInventario() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            ResponseEntity<InventarioResponseJsonApiDTO> responseExitoso =
                    new ResponseEntity<>(inventarioResponseValido, HttpStatus.CREATED);

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Error", null, "Error 1".getBytes(), null))
                    .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Error", null, "Error 2".getBytes(), null))
                    .thenReturn(responseExitoso);

            // When
            InventarioResponseJsonApiDTO resultado = inventarioClient.crearInventarioConReintentos(productoId, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            verify(restTemplate, times(3)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe manejar response con status 200 OK")
        void crearInventarioConReintentos_status200_retornaInventario() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            ResponseEntity<InventarioResponseJsonApiDTO> response =
                    new ResponseEntity<>(inventarioResponseValido, HttpStatus.OK);

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenReturn(response);

            // When
            InventarioResponseJsonApiDTO resultado = inventarioClient.crearInventarioConReintentos(productoId, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            verify(restTemplate, times(1)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe incluir headers correctos en la petición")
        void crearInventarioConReintentos_incluyeHeadersCorrectos() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            ResponseEntity<InventarioResponseJsonApiDTO> response =
                    new ResponseEntity<>(inventarioResponseValido, HttpStatus.CREATED);

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenReturn(response);

            // When
            inventarioClient.crearInventarioConReintentos(productoId, cantidad);

            // Then
            verify(restTemplate).postForEntity(
                    eq(url),
                    argThat(entity -> {
                        HttpHeaders headers = ((HttpEntity<?>) entity).getHeaders(); // Cast corregido
                        return API_KEY.equals(headers.getFirst("X-API-KEY")) &&
                                MediaType.APPLICATION_JSON.equals(headers.getContentType());
                    }),
                    eq(InventarioResponseJsonApiDTO.class)
            );
        }
    }

    @Nested
    @DisplayName("Tests de crearInventarioConReintentos - Casos de fallo")
    class CrearInventarioFallosTests {

        @Test
        @DisplayName("Debe lanzar InventarioException tras 3 intentos fallidos con HttpServerErrorException")
        void crearInventarioConReintentos_falla3Intentos_lanzaInventarioException() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenThrow(HttpServerErrorException.create(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error persistente",
                            null,
                            "Service error".getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.crearInventarioConReintentos(productoId, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("No se pudo crear el inventario para el producto");

            verify(restTemplate, times(3)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe lanzar InventarioException tras 3 intentos fallidos con HttpClientErrorException")
        void crearInventarioConReintentos_falla3IntentosClientError_lanzaInventarioException() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenThrow(HttpClientErrorException.create(
                            HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            null,
                            "Invalid data".getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.crearInventarioConReintentos(productoId, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("No se pudo crear el inventario para el producto");

            verify(restTemplate, times(3)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe incluir mensaje de última excepción en InventarioException")
        void crearInventarioConReintentos_incluyeMensajeError() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";
            String mensajeError = "Timeout al conectar con base de datos";

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenThrow(new RuntimeException(mensajeError));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.crearInventarioConReintentos(productoId, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining(mensajeError);
        }

        @Test
        @DisplayName("Debe esperar entre reintentos")
        void crearInventarioConReintentos_esperaEntreReintentos() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenThrow(new RuntimeException("Error"));

            long startTime = System.currentTimeMillis();

            // When
            try {
                inventarioClient.crearInventarioConReintentos(productoId, cantidad);
            } catch (InventarioException e) {
                // Expected
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then - debe haber esperado al menos 1000ms (2 delays de 500ms)
            assertThat(duration).isGreaterThanOrEqualTo(1000);
            verify(restTemplate, times(3)).postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class));
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando response no es 2xx")
        void crearInventarioConReintentos_responseNo2xx_lanzaInventarioException() {
            // Given
            Long productoId = 1L;
            Integer cantidad = 10;
            String url = API_URL + "inventarios";

            ResponseEntity<InventarioResponseJsonApiDTO> response =
                    new ResponseEntity<>(inventarioResponseValido, HttpStatus.ACCEPTED);

            when(restTemplate.postForEntity(eq(url), any(HttpEntity.class), eq(InventarioResponseJsonApiDTO.class)))
                    .thenReturn(response);

            // When
            InventarioResponseJsonApiDTO resultado = inventarioClient.crearInventarioConReintentos(productoId, cantidad);

            // Then
            assertThat(resultado).isNotNull();
        }
    }

    @Nested
    @DisplayName("Tests de obtenerInventariosDesdeMicroservicio")
    class ObtenerInventariosTests {

        @Test
        @DisplayName("Debe obtener inventarios exitosamente")
        void obtenerInventariosDesdeMicroservicio_exitoso_retornaLista() {
            // Given
            List<Long> productoIds = List.of(1L, 2L, 3L);
            String url = API_URL + "inventarios/buscar";

            ResponseEntity<InventarioListResponseJsonApiDTO> response =
                    new ResponseEntity<>(inventarioListResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            )).thenReturn(response);

            // When
            List<InventarioResponseJsonApiDTO.Data> resultado =
                    inventarioClient.obtenerInventariosDesdeMicroservicio(productoIds);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getId()).isEqualTo("1");
            verify(restTemplate).exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            );
        }

        @Test
        @DisplayName("Debe manejar lista vacía de IDs")
        void obtenerInventariosDesdeMicroservicio_listaVacia_retornaListaVacia() {
            // Given
            List<Long> productoIds = Collections.emptyList();
            String url = API_URL + "inventarios/buscar";

            InventarioListResponseJsonApiDTO emptyResponse = new InventarioListResponseJsonApiDTO();
            emptyResponse.setData(Collections.emptyList());

            ResponseEntity<InventarioListResponseJsonApiDTO> response =
                    new ResponseEntity<>(emptyResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            )).thenReturn(response);

            // When
            List<InventarioResponseJsonApiDTO.Data> resultado =
                    inventarioClient.obtenerInventariosDesdeMicroservicio(productoIds);

            // Then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe incluir headers correctos")
        void obtenerInventariosDesdeMicroservicio_incluyeHeadersCorrectos() {
            // Given
            List<Long> productoIds = List.of(1L);
            String url = API_URL + "inventarios/buscar";

            ResponseEntity<InventarioListResponseJsonApiDTO> response =
                    new ResponseEntity<>(inventarioListResponse, HttpStatus.OK);

            when(restTemplate.exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            )).thenReturn(response);

            // When
            inventarioClient.obtenerInventariosDesdeMicroservicio(productoIds);

            // Then
            verify(restTemplate).exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    argThat(entity -> {
                        HttpHeaders headers = entity.getHeaders();
                        return API_KEY.equals(headers.getFirst("X-API-KEY")) &&
                                MediaType.APPLICATION_JSON.equals(headers.getContentType());
                    }),
                    eq(InventarioListResponseJsonApiDTO.class)
            );
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando ocurre HttpServerErrorException")
        void obtenerInventariosDesdeMicroservicio_httpServerError_lanzaInventarioException() {
            // Given
            List<Long> productoIds = List.of(1L);
            String url = API_URL + "inventarios/buscar";

            when(restTemplate.exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            )).thenThrow(HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Error",
                    null,
                    "Database connection failed".getBytes(),
                    null
            ));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.obtenerInventariosDesdeMicroservicio(productoIds))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Fallo al conectar con el inventario");
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando ocurre HttpClientErrorException")
        void obtenerInventariosDesdeMicroservicio_httpClientError_lanzaInventarioException() {
            // Given
            List<Long> productoIds = List.of(1L);
            String url = API_URL + "inventarios/buscar";

            when(restTemplate.exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            )).thenThrow(HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND,
                    "Not Found",
                    null,
                    "Inventario not found".getBytes(),
                    null
            ));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.obtenerInventariosDesdeMicroservicio(productoIds))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Fallo al conectar con el inventario");
        }

        @Test
        @DisplayName("Debe incluir response body en excepción")
        void obtenerInventariosDesdeMicroservicio_incluyeResponseBodyEnExcepcion() {
            // Given
            List<Long> productoIds = List.of(1L);
            String url = API_URL + "inventarios/buscar";
            String errorBody = "Error details from server";

            when(restTemplate.exchange(
                    eq(url),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(InventarioListResponseJsonApiDTO.class)
            )).thenThrow(HttpServerErrorException.create(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service Unavailable",
                    null,
                    errorBody.getBytes(),
                    null
            ));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.obtenerInventariosDesdeMicroservicio(productoIds))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Fallo al conectar con el inventario");
        }
    }

    @Nested
    @DisplayName("Tests de actualizarInventario")
    class ActualizarInventarioTests {

        @Test
        @DisplayName("Debe actualizar inventario exitosamente")
        void actualizarInventario_exitoso_noLanzaExcepcion() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = 20;
            String url = API_URL + "inventarios";

            doNothing().when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When & Then - no debe lanzar excepción
            inventarioClient.actualizarInventario(productoId, nuevaCantidad);

            verify(restTemplate).put(eq(url), any(HttpEntity.class));
        }

        @Test
        @DisplayName("Debe incluir headers correctos en actualización")
        void actualizarInventario_incluyeHeadersCorrectos() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = 20;
            String url = API_URL + "inventarios";

            doNothing().when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When
            inventarioClient.actualizarInventario(productoId, nuevaCantidad);

            // Then
            verify(restTemplate).put(
                    eq(url),
                    argThat(entity -> {
                        HttpHeaders headers = ((HttpEntity<?>) entity).getHeaders(); // Cast corregido
                        return API_KEY.equals(headers.getFirst("X-API-KEY")) &&
                                MediaType.APPLICATION_JSON.equals(headers.getContentType());
                    })
            );
        }

        @Test
        @DisplayName("Debe incluir body correcto con productoId y cantidad")
        void actualizarInventario_incluyeBodyCorrecto() {
            // Given
            Long productoId = 5L;
            Integer nuevaCantidad = 15;
            String url = API_URL + "inventarios";

            doNothing().when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When
            inventarioClient.actualizarInventario(productoId, nuevaCantidad);

            // Then
            verify(restTemplate).put(
                    eq(url),
                    argThat(entity -> {
                        InventarioRequestJsonApiDTO body = (InventarioRequestJsonApiDTO) ((HttpEntity<?>) entity).getBody(); // Cast corregido
                        return body != null &&
                                body.getData() != null &&
                                body.getData().getAttributes() != null &&
                                body.getData().getAttributes().getProductoId().equals(productoId) &&
                                body.getData().getAttributes().getCantidadDisponible().equals(nuevaCantidad); // Estructura correcta
                    })
            );
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando ocurre HttpServerErrorException")
        void actualizarInventario_httpServerError_lanzaInventarioException() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = 20;
            String url = API_URL + "inventarios";

            doThrow(HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Error",
                    null,
                    "Update failed".getBytes(),
                    null
            )).when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.actualizarInventario(productoId, nuevaCantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Fallo al actualizar inventario");
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando ocurre HttpClientErrorException")
        void actualizarInventario_httpClientError_lanzaInventarioException() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = 20;
            String url = API_URL + "inventarios";

            doThrow(HttpClientErrorException.create(
                    HttpStatus.BAD_REQUEST,
                    "Bad Request",
                    null,
                    "Invalid cantidad".getBytes(),
                    null
            )).when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.actualizarInventario(productoId, nuevaCantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Fallo al actualizar inventario");
        }

        @Test
        @DisplayName("Debe actualizar con cantidad cero")
        void actualizarInventario_cantidadCero_actualiza() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = 0;
            String url = API_URL + "inventarios";

            doNothing().when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When
            inventarioClient.actualizarInventario(productoId, nuevaCantidad);

            // Then
            verify(restTemplate).put(eq(url), any(HttpEntity.class));
        }

        @Test
        @DisplayName("Debe actualizar con cantidad negativa")
        void actualizarInventario_cantidadNegativa_actualiza() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = -5;
            String url = API_URL + "inventarios";

            doNothing().when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When
            inventarioClient.actualizarInventario(productoId, nuevaCantidad);

            // Then
            verify(restTemplate).put(eq(url), any(HttpEntity.class));
        }

        @Test
        @DisplayName("Debe incluir response body en excepción")
        void actualizarInventario_incluyeResponseBodyEnExcepcion() {
            // Given
            Long productoId = 1L;
            Integer nuevaCantidad = 20;
            String url = API_URL + "inventarios";
            String errorBody = "Producto no encontrado en inventario";

            doThrow(HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND,
                    "Not Found",
                    null,
                    errorBody.getBytes(),
                    null
            )).when(restTemplate).put(eq(url), any(HttpEntity.class));

            // When & Then
            assertThatThrownBy(() -> inventarioClient.actualizarInventario(productoId, nuevaCantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Fallo al actualizar inventario");
        }
    }

}