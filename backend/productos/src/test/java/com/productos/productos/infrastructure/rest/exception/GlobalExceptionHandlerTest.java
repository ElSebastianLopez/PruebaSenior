package com.productos.productos.infrastructure.rest.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler - Tests Unitarios")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Tests de manejarEntidadNoEncontrada")
    class ManejarEntidadNoEncontradaTests {

        @Test
        @DisplayName("Debe retornar 404 con estructura JSON:API correcta")
        void manejarEntidadNoEncontrada_retorna404ConEstructuraCorrecta() {
            // Arrange
            EntityNotFoundException exception = new EntityNotFoundException("Producto no encontrado con ID: 999");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarEntidadNoEncontrada(exception);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());

            Map<String, Object> body = response.getBody();
            assertTrue(body.containsKey("errors"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) body.get("errors");
            assertEquals(1, errors.size());

            Map<String, String> error = errors.get(0);
            assertEquals("404", error.get("status"));
            assertEquals("Recurso no encontrado", error.get("title"));
            assertEquals("Producto no encontrado con ID: 999", error.get("detail"));
        }

        @Test
        @DisplayName("Debe manejar mensaje de excepción vacío")
        void manejarEntidadNoEncontrada_mensajeVacio_retorna404() {
            // Arrange
            EntityNotFoundException exception = new EntityNotFoundException("");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarEntidadNoEncontrada(exception);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Tests de manejarErroresValidacion")
    class ManejarErroresValidacionTests {

        @Test
        @DisplayName("Debe retornar 400 con lista de errores de validación")
        void manejarErroresValidacion_retorna400ConListaErrores() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("producto", "nombre", "no debe estar vacío");
            FieldError fieldError2 = new FieldError("producto", "precio", "debe ser mayor que 0");

            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarErroresValidacion(exception);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());

            Map<String, Object> body = response.getBody();
            assertTrue(body.containsKey("errors"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) body.get("errors");
            assertEquals(2, errors.size());

            Map<String, String> error1 = errors.get(0);
            assertEquals("Error de validación", error1.get("title"));
            assertTrue(error1.get("detail").contains("nombre"));
            assertTrue(error1.get("detail").contains("no debe estar vacío"));

            Map<String, String> error2 = errors.get(1);
            assertEquals("Error de validación", error2.get("title"));
            assertTrue(error2.get("detail").contains("precio"));
            assertTrue(error2.get("detail").contains("debe ser mayor que 0"));
        }

        @Test
        @DisplayName("Debe manejar lista vacía de errores de validación")
        void manejarErroresValidacion_listaVacia_retorna400() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarErroresValidacion(exception);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) response.getBody().get("errors");
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Debe manejar un solo error de validación")
        void manejarErroresValidacion_unSoloError_retorna400() {
            // Arrange
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError = new FieldError("producto", "categoria", "es requerida");

            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

            MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarErroresValidacion(exception);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) response.getBody().get("errors");
            assertEquals(1, errors.size());
        }
    }

    @Nested
    @DisplayName("Tests de manejarRuntime")
    class ManejarRuntimeTests {

        @Test
        @DisplayName("Debe retornar 500 con estructura JSON:API correcta")
        void manejarRuntime_retorna500ConEstructuraCorrecta() {
            // Arrange
            RuntimeException exception = new RuntimeException("Error inesperado del sistema");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarRuntime(exception);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());

            Map<String, Object> body = response.getBody();
            assertTrue(body.containsKey("errors"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) body.get("errors");
            assertEquals(1, errors.size());

            Map<String, String> error = errors.get(0);
            assertEquals("500", error.get("status"));
            assertEquals("Error interno del servidor", error.get("title"));
            assertEquals("Error inesperado del sistema", error.get("detail"));
        }

        @Test
        @DisplayName("Debe manejar RuntimeException con mensaje null")
        void manejarRuntime_mensajeNull_retorna500() {
            // Arrange
            RuntimeException exception = new RuntimeException((String) null);

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarRuntime(exception);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Tests de manejarArgumentoInvalido")
    class ManejarArgumentoInvalidoTests {

        @Test
        @DisplayName("Debe retornar 400 con estructura JSON:API correcta")
        void manejarArgumentoInvalido_retorna400ConEstructuraCorrecta() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Valor de precio inválido");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarArgumentoInvalido(exception);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());

            Map<String, Object> body = response.getBody();
            assertTrue(body.containsKey("errors"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) body.get("errors");
            assertEquals(1, errors.size());

            Map<String, String> error = errors.get(0);
            assertEquals("400", error.get("status"));
            assertEquals("Argumento inválido", error.get("title"));
            assertEquals("Valor de precio inválido", error.get("detail"));
        }

        @Test
        @DisplayName("Debe manejar IllegalArgumentException con mensaje vacío")
        void manejarArgumentoInvalido_mensajeVacio_retorna400() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("");

            // Act
            ResponseEntity<Map<String, Object>> response = handler.manejarArgumentoInvalido(exception);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Tests de manejarErrorInventario")
    class ManejarErrorInventarioTests {

        @Test
        @DisplayName("Debe parsear y retornar respuesta del microservicio de inventario")
        void manejarErrorInventario_parseoExitoso_retornaRespuestaOriginal() {
            // Arrange
            String jsonResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Cantidad inválida\",\"detail\":\"La cantidad debe ser mayor a 0\"}]}";
            InventarioException exception = new InventarioException(
                    "Error al crear inventario: " + jsonResponse,
                    jsonResponse
            );

            // Act
            ResponseEntity<?> response = handler.manejarErrorInventario(exception);

            // Assert
            assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Map);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(body.containsKey("errors"));
        }

        @Test
        @DisplayName("Debe retornar error estándar cuando falla el parseo")
        void manejarErrorInventario_parseoFallido_retornaErrorEstandar() {
            // Arrange
            InventarioException exception = new InventarioException(
                    "Error de conexión con inventario",
                    "invalid json {{{("
            );

            // Act
            ResponseEntity<?> response = handler.manejarErrorInventario(exception);

            // Assert
            assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Map);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(body.containsKey("errors"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) body.get("errors");
            assertEquals(1, errors.size());
            assertEquals("502", errors.get(0).get("status"));
            assertEquals("Error al interactuar con Inventario", errors.get(0).get("title"));
        }

        @Test
        @DisplayName("Debe manejar InventarioException sin mensaje JSON embebido")
        void manejarErrorInventario_sinJsonEmbebido_retornaErrorEstandar() {
            // Arrange
            InventarioException exception = new InventarioException(
                    "Timeout al conectar con inventario",
                    null
            );

            // Act
            ResponseEntity<?> response = handler.manejarErrorInventario(exception);

            // Assert
            assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Debe manejar JSON válido pero sin estructura esperada")
        void manejarErrorInventario_jsonValidoSinEstructura_parsea() {
            // Arrange
            String jsonResponse = "{\"mensaje\":\"Error genérico\"}";
            InventarioException exception = new InventarioException(
                    "Error al crear inventario: " + jsonResponse,
                    jsonResponse
            );

            // Act
            ResponseEntity<?> response = handler.manejarErrorInventario(exception);

            // Assert
            assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Map);
        }
    }

    @Nested
    @DisplayName("Tests de Integración de Estructura de Respuesta")
    class EstructuraRespuestaTests {

        @Test
        @DisplayName("Todas las respuestas de error deben tener estructura consistente")
        void todasLasRespuestas_tienenEstructuraConsistente() {
            // Arrange & Act
            ResponseEntity<Map<String, Object>> response404 = handler.manejarEntidadNoEncontrada(
                    new EntityNotFoundException("Test")
            );
            ResponseEntity<Map<String, Object>> response400 = handler.manejarArgumentoInvalido(
                    new IllegalArgumentException("Test")
            );
            ResponseEntity<Map<String, Object>> response500 = handler.manejarRuntime(
                    new RuntimeException("Test")
            );

            // Assert - Todas tienen campo "errors"
            assertNotNull(response404.getBody());
            assertTrue(response404.getBody().containsKey("errors"));

            assertNotNull(response400.getBody());
            assertTrue(response400.getBody().containsKey("errors"));

            assertNotNull(response500.getBody());
            assertTrue(response500.getBody().containsKey("errors"));
        }

        @Test
        @DisplayName("Los errores deben contener campos status, title y detail")
        void errores_contienenCamposRequeridos() {
            // Arrange & Act
            ResponseEntity<Map<String, Object>> response = handler.manejarEntidadNoEncontrada(
                    new EntityNotFoundException("Test")
            );

            // Assert
            @SuppressWarnings("unchecked")
            List<Map<String, String>> errors = (List<Map<String, String>>) response.getBody().get("errors");
            Map<String, String> error = errors.get(0);

            assertTrue(error.containsKey("status"));
            assertTrue(error.containsKey("title"));
            assertTrue(error.containsKey("detail"));
            assertNotNull(error.get("status"));
            assertNotNull(error.get("title"));
            assertNotNull(error.get("detail"));
        }
    }
}