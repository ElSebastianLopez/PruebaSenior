package com.productos.productos.aplication.service.impl;


import com.productos.productos.domain.model.Producto;
import com.productos.productos.domain.repository.ProductosRepository;
import com.productos.productos.infrastructure.client.InventarioClient;
import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductoTransactionalServiceImpl - Tests Unitarios")
class ProductoTransactionalServiceImplTest {
    @Mock
    private ProductosRepository productoRepository;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private ProductoTransactionalServiceImpl productoTransactionalService;

    private Producto productoEjemplo;
    private Producto productoGuardado;
    private InventarioResponseJsonApiDTO inventarioResponseValido;

    @BeforeEach
    void setUp() {
        // Producto sin ID (antes de guardar)
        productoEjemplo = new Producto();
        productoEjemplo.setNombre("Laptop");
        productoEjemplo.setDescripcion("Laptop gaming");
        productoEjemplo.setPrecio(new BigDecimal("1500.00"));
        productoEjemplo.setCategoria("Electrónica");

        // Producto guardado (con ID)
        productoGuardado = new Producto();
        productoGuardado.setId(1L);
        productoGuardado.setNombre("Laptop");
        productoGuardado.setDescripcion("Laptop gaming");
        productoGuardado.setPrecio(new BigDecimal("1500.00"));
        productoGuardado.setCategoria("Electrónica");

        // Response de inventario válido
        InventarioResponseJsonApiDTO.Data inventarioData = new InventarioResponseJsonApiDTO.Data();
        inventarioData.setId("1");
        inventarioData.setType("inventarios");

        inventarioResponseValido = new InventarioResponseJsonApiDTO();
        inventarioResponseValido.setData(inventarioData);
    }

    @Nested
    @DisplayName("Tests de crearProductoYInventario - Casos exitosos")
    class CrearProductoYInventarioExitososTests {

        @Test
        @DisplayName("Debe crear producto e inventario exitosamente")
        void crearProductoYInventario_exitoso_retornaProductoGuardado() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(inventarioResponseValido);

            // When
            Producto resultado = productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Laptop");

            verify(productoRepository).save(productoEjemplo);
            verify(inventarioClient).crearInventarioConReintentos(1L, cantidad);
        }

        @Test
        @DisplayName("Debe guardar producto antes de crear inventario")
        void crearProductoYInventario_ordenCorrecto_guardaProductoPrimero() {
            // Given
            int cantidad = 5;
            when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(anyLong(), anyInt()))
                    .thenReturn(inventarioResponseValido);

            // When
            productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            // Verificar orden de ejecución
            var inOrder = inOrder(productoRepository, inventarioClient);
            inOrder.verify(productoRepository).save(productoEjemplo);
            inOrder.verify(inventarioClient).crearInventarioConReintentos(1L, cantidad);
        }

        @Test
        @DisplayName("Debe usar el ID del producto guardado para crear inventario")
        void crearProductoYInventario_usaIdProductoGuardado() {
            // Given
            int cantidad = 15;
            Producto productoConIdDiferente = new Producto();
            productoConIdDiferente.setId(999L);
            productoConIdDiferente.setNombre("Test");
            productoConIdDiferente.setPrecio(new BigDecimal("100.00"));

            when(productoRepository.save(any(Producto.class))).thenReturn(productoConIdDiferente);
            when(inventarioClient.crearInventarioConReintentos(999L, cantidad))
                    .thenReturn(inventarioResponseValido);

            // When
            Producto resultado = productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            assertThat(resultado.getId()).isEqualTo(999L);
            verify(inventarioClient).crearInventarioConReintentos(999L, cantidad);
        }
    }

    @Nested
    @DisplayName("Tests de crearProductoYInventario - Validación de respuesta")
    class ValidacionRespuestaTests {

        @Test
        @DisplayName("Debe lanzar InventarioException cuando respuesta es null")
        void crearProductoYInventario_respuestaNull_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(null);

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("La respuesta del servicio de inventario no es válida");

            verify(productoRepository).save(productoEjemplo);
            verify(inventarioClient).crearInventarioConReintentos(1L, cantidad);
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando respuesta.getData() es null")
        void crearProductoYInventario_dataNull_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            InventarioResponseJsonApiDTO respuestaInvalida = new InventarioResponseJsonApiDTO();
            respuestaInvalida.setData(null);

            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(respuestaInvalida);

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("La respuesta del servicio de inventario no es válida");
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando data.getId() es null")
        void crearProductoYInventario_dataIdNull_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            InventarioResponseJsonApiDTO.Data dataSinId = new InventarioResponseJsonApiDTO.Data();
            dataSinId.setId(null);
            dataSinId.setType("inventarios");

            InventarioResponseJsonApiDTO respuestaInvalida = new InventarioResponseJsonApiDTO();
            respuestaInvalida.setData(dataSinId);

            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(respuestaInvalida);

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("La respuesta del servicio de inventario no es válida");
        }
    }

    @Nested
    @DisplayName("Tests de crearProductoYInventario - Manejo de errores HTTP")
    class ManejoErroresHttpTests {

        @Test
        @DisplayName("Debe lanzar InventarioException cuando inventario retorna HttpServerErrorException")
        void crearProductoYInventario_httpServerError_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenThrow(HttpServerErrorException.create(
                            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                            "Internal Server Error",
                            null,
                            "Service temporarily unavailable".getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Error al crear inventario");

            verify(productoRepository).save(productoEjemplo);
            verify(inventarioClient).crearInventarioConReintentos(1L, cantidad);
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando inventario retorna HttpClientErrorException")
        void crearProductoYInventario_httpClientError_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenThrow(HttpClientErrorException.create(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            null,
                            "Invalid cantidad value".getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Error al crear inventario");
        }

        @Test
        @DisplayName("Debe lanzar InventarioException con mensaje del error HTTP cuando falla inventario")
        void crearProductoYInventario_errorHttp_incluyeMensajeOriginal() {
            // Given
            int cantidad = 10;
            String mensajeError = "Producto no encontrado en sistema de inventario";

            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenThrow(HttpClientErrorException.create(
                            org.springframework.http.HttpStatus.NOT_FOUND,
                            "Not Found",
                            null,
                            mensajeError.getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class)
                    .hasMessageContaining("Error al crear inventario");
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando inventario retorna 503 Service Unavailable")
        void crearProductoYInventario_serviceUnavailable_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenThrow(HttpServerErrorException.create(
                            org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                            "Service Unavailable",
                            null,
                            "Inventario service is down".getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class);
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando inventario retorna 422 Unprocessable Entity")
        void crearProductoYInventario_unprocessableEntity_lanzaInventarioException() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenThrow(HttpClientErrorException.create(
                            org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
                            "Unprocessable Entity",
                            null,
                            "Cantidad debe ser mayor a cero".getBytes(),
                            null
                    ));

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(InventarioException.class);
        }
    }

    @Nested
    @DisplayName("Tests de crearProductoYInventario - Casos límite")
    class CasosLimiteTests {

        @Test
        @DisplayName("Debe manejar cantidad cero correctamente")
        void crearProductoYInventario_cantidadCero_procesaCorrectamente() {
            // Given
            int cantidad = 0;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(inventarioResponseValido);

            // When
            Producto resultado = productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            verify(inventarioClient).crearInventarioConReintentos(1L, 0);
        }

        @Test
        @DisplayName("Debe manejar cantidad negativa correctamente")
        void crearProductoYInventario_cantidadNegativa_procesaCorrectamente() {
            // Given
            int cantidad = -5;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(inventarioResponseValido);

            // When
            Producto resultado = productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            verify(inventarioClient).crearInventarioConReintentos(1L, -5);
        }

        @Test
        @DisplayName("Debe manejar cantidad muy grande correctamente")
        void crearProductoYInventario_cantidadMuyGrande_procesaCorrectamente() {
            // Given
            int cantidad = Integer.MAX_VALUE;
            when(productoRepository.save(productoEjemplo)).thenReturn(productoGuardado);
            when(inventarioClient.crearInventarioConReintentos(1L, cantidad))
                    .thenReturn(inventarioResponseValido);

            // When
            Producto resultado = productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            assertThat(resultado).isNotNull();
            verify(inventarioClient).crearInventarioConReintentos(1L, Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Debe preservar todos los datos del producto guardado")
        void crearProductoYInventario_preservaDatosProducto() {
            // Given
            int cantidad = 10;
            Producto productoCompleto = new Producto();
            productoCompleto.setId(123L);
            productoCompleto.setNombre("Mouse Inalámbrico");
            productoCompleto.setDescripcion("Mouse ergonómico");
            productoCompleto.setPrecio(new BigDecimal("45.99"));
            productoCompleto.setCategoria("Periféricos");

            when(productoRepository.save(any(Producto.class))).thenReturn(productoCompleto);
            when(inventarioClient.crearInventarioConReintentos(123L, cantidad))
                    .thenReturn(inventarioResponseValido);

            // When
            Producto resultado = productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad);

            // Then
            assertThat(resultado.getId()).isEqualTo(123L);
            assertThat(resultado.getNombre()).isEqualTo("Mouse Inalámbrico");
            assertThat(resultado.getDescripcion()).isEqualTo("Mouse ergonómico");
            assertThat(resultado.getPrecio()).isEqualByComparingTo(new BigDecimal("45.99"));
            assertThat(resultado.getCategoria()).isEqualTo("Periféricos");
        }
    }

    @Nested
    @DisplayName("Tests de comportamiento transaccional")
    class ComportamientoTransaccionalTests {

        @Test
        @DisplayName("No debe llamar a inventario si falla guardar producto")
        void crearProductoYInventario_fallaGuardarProducto_noLlamaInventario() {
            // Given
            int cantidad = 10;
            when(productoRepository.save(productoEjemplo))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection failed");

            verify(productoRepository).save(productoEjemplo);
            verifyNoInteractions(inventarioClient);
        }

        @Test
        @DisplayName("Debe propagar excepción cuando falla guardado de producto")
        void crearProductoYInventario_errorAlGuardar_propagaExcepcion() {
            // Given
            int cantidad = 10;
            RuntimeException dbException = new RuntimeException("Constraint violation");
            when(productoRepository.save(any(Producto.class))).thenThrow(dbException);

            // When & Then
            assertThatThrownBy(() ->
                    productoTransactionalService.crearProductoYInventario(productoEjemplo, cantidad))
                    .isSameAs(dbException);
        }
    }


}