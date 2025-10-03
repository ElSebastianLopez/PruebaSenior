package com.productos.productos.aplication.service.impl;

import com.productos.productos.aplication.service.ProductoTransactionalService;
import com.productos.productos.domain.model.Producto;
import com.productos.productos.domain.repository.ProductosRepository;
import com.productos.productos.infrastructure.client.InventarioClient;
import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;
import com.productos.productos.shared.dto.pageable.PageableRequest;
import com.productos.productos.shared.mapper.ProductoMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductosServiceImpl - Tests Unitarios")
class ProductosServiceImplTest {
    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private InventarioClient inventarioClient;

    @Mock
    private ProductoTransactionalService productoTransactionalService;

    @InjectMocks
    private ProductosServiceImpl productosService;

    private Producto producto;
    private ProductoRequestJsonApiDTO productoRequest;
    private ProductoResponseJsonApiDTO.Data productoResponseData;
    private InventarioResponseJsonApiDTO.Data inventarioData;

    @BeforeEach
    void setUp() {
        // Setup Producto
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setDescripcion("Laptop gaming");
        producto.setPrecio(new BigDecimal("1500.00"));
        producto.setCategoria("Electrónica");

        // Setup Request DTO
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

        // Setup Response Data
        ProductoResponseJsonApiDTO.Data.Attributes responseAttributes = new ProductoResponseJsonApiDTO.Data.Attributes();
        responseAttributes.setNombre("Laptop");
        responseAttributes.setDescripcion("Laptop gaming");
        responseAttributes.setPrecio(new BigDecimal("1500.00"));
        responseAttributes.setCategoria("Electrónica");
        responseAttributes.setCantidad(10);

        productoResponseData = new ProductoResponseJsonApiDTO.Data();
        productoResponseData.setId("1");
        productoResponseData.setType("producto");
        productoResponseData.setAttributes(responseAttributes);

        // Setup Inventario Data
        InventarioResponseJsonApiDTO.Data.Attributes inventarioAttributes = new InventarioResponseJsonApiDTO.Data.Attributes();
        inventarioAttributes.setProductoId(1L);
        inventarioAttributes.setCantidadDisponible(10);

        inventarioData = new InventarioResponseJsonApiDTO.Data();
        inventarioData.setId("1");
        inventarioData.setAttributes(inventarioAttributes);
    }

    @Nested
    @DisplayName("Tests de getTodosLosProductos")
    class GetTodosLosProductosTests {

        @Test
        @DisplayName("Debe retornar lista de productos con inventario exitosamente")
        void getTodosLosProductos_filtrosValidos_retornaListaConInventario() {
            // Arrange
            ProductoFiltroDTO filtro = new ProductoFiltroDTO();
            PageableRequest pageableRequest = new PageableRequest();
            pageableRequest.setPage(0);
            pageableRequest.setSize(10);
            pageableRequest.setOrder("asc");
            filtro.setPageable(pageableRequest);

            Page<Producto> productosPage = new PageImpl<>(List.of(producto));

            when(productosRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(productosPage);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(any(Producto.class), anyInt()))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.getTodosLosProductos(filtro, "http://localhost:8080");

            // Assert
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals(1, result.getData().size());
            verify(productosRepository).findAll(any(Specification.class), any(Pageable.class));
            verify(inventarioClient).obtenerInventariosDesdeMicroservicio(anyList());
            verify(productoMapper).toJsonApiDTOData(producto, 10);
        }

        @Test
        @DisplayName("Debe aplicar orden descendente cuando se especifica")
        void getTodosLosProductos_ordenDesc_aplicaOrdenCorrectamente() {
            // Arrange
            ProductoFiltroDTO filtro = new ProductoFiltroDTO();
            PageableRequest pageableRequest = new PageableRequest();
            pageableRequest.setPage(0);
            pageableRequest.setSize(10);
            pageableRequest.setOrder("desc");
            filtro.setPageable(pageableRequest);

            Page<Producto> productosPage = new PageImpl<>(List.of(producto));

            when(productosRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(productosPage);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(any(Producto.class), anyInt()))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.getTodosLosProductos(filtro, "http://localhost:8080");

            // Assert
            assertNotNull(result);
            verify(productosRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Debe retornar null en cantidad cuando inventario no existe")
        void getTodosLosProductos_inventarioNoExiste_retornaNullEnCantidad() {
            // Arrange
            ProductoFiltroDTO filtro = new ProductoFiltroDTO();
            PageableRequest pageableRequest = new PageableRequest();
            pageableRequest.setPage(0);
            pageableRequest.setSize(10);
            pageableRequest.setOrder("asc");
            filtro.setPageable(pageableRequest);

            Page<Producto> productosPage = new PageImpl<>(List.of(producto));

            when(productosRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(productosPage);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of());
            when(productoMapper.toJsonApiDTOData(any(Producto.class), isNull()))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.getTodosLosProductos(filtro, "http://localhost:8080");

            // Assert
            assertNotNull(result);
            verify(productoMapper).toJsonApiDTOData(producto, null);
        }
    }

    @Nested
    @DisplayName("Tests de getProductoPorId")
    class GetProductoPorIdTests {

        @Test
        @DisplayName("Debe retornar producto cuando existe")
        void getProductoPorId_productoExiste_retornaProductoConInventario() {
            // Arrange
            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(producto, 10))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.getProductoPorId(1L);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals(1, result.getData().size());
            assertEquals("1", result.getData().get(0).getId());
            verify(productosRepository).findById(1L);
            verify(inventarioClient).obtenerInventariosDesdeMicroservicio(List.of(1L));
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando producto no existe")
        void getProductoPorId_productoNoExiste_lanzaExcepcion() {
            // Arrange
            when(productosRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> productosService.getProductoPorId(999L)
            );

            assertTrue(exception.getMessage().contains("Producto no encontrado con ID: 999"));
            verify(productosRepository).findById(999L);
            verifyNoInteractions(inventarioClient);
        }

        @Test
        @DisplayName("Debe retornar producto con cantidad null cuando inventario no existe")
        void getProductoPorId_inventarioNoExiste_retornaCantidadNull() {
            // Arrange
            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of());
            when(productoMapper.toJsonApiDTOData(producto, null))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.getProductoPorId(1L);

            // Assert
            assertNotNull(result);
            verify(productoMapper).toJsonApiDTOData(producto, null);
        }
    }

    @Nested
    @DisplayName("Tests de crearProducto")
    class CrearProductoTests {

        @Test
        @DisplayName("Debe crear producto exitosamente con inventario")
        void crearProducto_datosValidos_creaProductoYInventario() {
            // Arrange
            when(productoMapper.fromCreateDTO(productoRequest)).thenReturn(producto);
            when(productoTransactionalService.crearProductoYInventario(any(Producto.class), anyInt()))
                    .thenReturn(producto);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(producto, 10))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.crearProducto(productoRequest);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals(1, result.getData().size());
            verify(productoMapper).fromCreateDTO(productoRequest);
            verify(productoTransactionalService).crearProductoYInventario(producto, 10);
            verify(inventarioClient).obtenerInventariosDesdeMicroservicio(anyList());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando hay violación de integridad")
        void crearProducto_violacionIntegridad_lanzaExcepcion() {
            // Arrange
            when(productoMapper.fromCreateDTO(productoRequest)).thenReturn(producto);
            when(productoTransactionalService.crearProductoYInventario(any(Producto.class), anyInt()))
                    .thenThrow(new DataIntegrityViolationException("Error de integridad"));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> productosService.crearProducto(productoRequest)
            );

            assertTrue(exception.getMessage().contains("Error de integridad al guardar el producto"));
            verify(productoTransactionalService).crearProductoYInventario(producto, 10);
            verifyNoInteractions(inventarioClient);
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando falla creación de inventario")
        void crearProducto_errorInventario_lanzaInventarioException() {
            // Arrange
            when(productoMapper.fromCreateDTO(productoRequest)).thenReturn(producto);
            when(productoTransactionalService.crearProductoYInventario(any(Producto.class), anyInt()))
                    .thenThrow(HttpClientErrorException.BadRequest.create(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Bad Request",
                            null,
                            "Error en inventario".getBytes(),
                            null
                    ));

            // Act & Assert
            InventarioException exception = assertThrows(
                    InventarioException.class,
                    () -> productosService.crearProducto(productoRequest)
            );

            assertTrue(exception.getMessage().contains("Fallo al crear inventario"));
        }

        @Test
        @DisplayName("Debe lanzar InventarioException cuando hay error de servidor en inventario")
        void crearProducto_errorServidorInventario_lanzaInventarioException() {
            // Arrange
            when(productoMapper.fromCreateDTO(productoRequest)).thenReturn(producto);
            when(productoTransactionalService.crearProductoYInventario(any(Producto.class), anyInt()))
                    .thenThrow(HttpServerErrorException.InternalServerError.create(
                            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                            "Internal Server Error",
                            null,
                            "Error servidor".getBytes(),
                            null
                    ));

            // Act & Assert
            assertThrows(InventarioException.class, () -> productosService.crearProducto(productoRequest));
        }
    }

    @Nested
    @DisplayName("Tests de actualizarProducto")
    class ActualizarProductoTests {

        @Test
        @DisplayName("Debe actualizar producto exitosamente sin cambiar inventario")
        void actualizarProducto_sinCambioInventario_actualizaSoloProducto() {
            // Arrange
            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productosRepository.save(any(Producto.class))).thenReturn(producto);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(producto, 10))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.actualizarProducto(1L, productoRequest);

            // Assert
            assertNotNull(result);
            verify(productosRepository).findById(1L);
            verify(productosRepository).save(producto);
            verify(inventarioClient, never()).actualizarInventario(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Debe actualizar producto y su inventario cuando cantidad cambia")
        void actualizarProducto_conCambioInventario_actualizaProductoEInventario() {
            // Arrange
            productoRequest.getData().getAttributes().setCantidad(20);

            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productosRepository.save(any(Producto.class))).thenReturn(producto);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(producto, 20))
                    .thenReturn(productoResponseData);

            // Act
            ProductoResponseJsonApiDTO result = productosService.actualizarProducto(1L, productoRequest);

            // Assert
            assertNotNull(result);
            verify(inventarioClient).actualizarInventario(1L, 20);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando producto no existe")
        void actualizarProducto_productoNoExiste_lanzaExcepcion() {
            // Arrange
            when(productosRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> productosService.actualizarProducto(999L, productoRequest)
            );

            assertTrue(exception.getMessage().contains("Producto no encontrado con ID: 999"));
            verify(productosRepository).findById(999L);
            verify(productosRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando hay violación de integridad")
        void actualizarProducto_violacionIntegridad_lanzaExcepcion() {
            // Arrange
            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productosRepository.save(any(Producto.class)))
                    .thenThrow(new DataIntegrityViolationException("Error de integridad"));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> productosService.actualizarProducto(1L, productoRequest)
            );

            assertTrue(exception.getMessage().contains("Error de integridad al actualizar el producto"));
        }

        @Test
        @DisplayName("Debe actualizar solo los campos del producto")
        void actualizarProducto_datosValidos_actualizaCamposCorrectamente() {
            // Arrange
            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            when(productosRepository.save(any(Producto.class))).thenReturn(producto);
            when(inventarioClient.obtenerInventariosDesdeMicroservicio(anyList()))
                    .thenReturn(List.of(inventarioData));
            when(productoMapper.toJsonApiDTOData(any(Producto.class), anyInt()))
                    .thenReturn(productoResponseData);

            // Act
            productosService.actualizarProducto(1L, productoRequest);

            // Assert
            assertEquals("Laptop", producto.getNombre());
            assertEquals("Laptop gaming", producto.getDescripcion());
            assertEquals(new BigDecimal("1500.00"), producto.getPrecio());
            assertEquals("Electrónica", producto.getCategoria());
        }
    }

    @Nested
    @DisplayName("Tests de eliminarProductoPorId")
    class EliminarProductoPorIdTests {

        @Test
        @DisplayName("Debe eliminar producto exitosamente cuando existe")
        void eliminarProductoPorId_productoExiste_eliminaCorrectamente() {
            // Arrange
            when(productosRepository.findById(1L)).thenReturn(Optional.of(producto));
            doNothing().when(productosRepository).delete(producto);

            // Act
            productosService.eliminarProductoPorId(1L);

            // Assert
            verify(productosRepository).findById(1L);
            verify(productosRepository).delete(producto);
        }

        @Test
        @DisplayName("Debe lanzar EntityNotFoundException cuando producto no existe")
        void eliminarProductoPorId_productoNoExiste_lanzaExcepcion() {
            // Arrange
            when(productosRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> productosService.eliminarProductoPorId(999L)
            );

            assertTrue(exception.getMessage().contains("Producto no encontrado con ID: 999"));
            verify(productosRepository).findById(999L);
            verify(productosRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Tests de obtenerCantidadDesdeInventario")
    class ObtenerCantidadDesdeInventarioTests {

        @Test
        @DisplayName("Debe retornar cantidad cuando inventario existe")
        void obtenerCantidadDesdeInventario_inventarioExiste_retornaCantidad() {
            // Act
            Integer cantidad = productosService.obtenerCantidadDesdeInventario(1L, List.of(inventarioData));

            // Assert
            assertEquals(10, cantidad);
        }

        @Test
        @DisplayName("Debe retornar null cuando inventario no existe")
        void obtenerCantidadDesdeInventario_inventarioNoExiste_retornaNull() {
            // Act
            Integer cantidad = productosService.obtenerCantidadDesdeInventario(999L, List.of(inventarioData));

            // Assert
            assertNull(cantidad);
        }

        @Test
        @DisplayName("Debe retornar null cuando lista de inventarios está vacía")
        void obtenerCantidadDesdeInventario_listaVacia_retornaNull() {
            // Act
            Integer cantidad = productosService.obtenerCantidadDesdeInventario(1L, List.of());

            // Assert
            assertNull(cantidad);
        }

        @Test
        @DisplayName("Debe manejar inventarios con attributes null")
        void obtenerCantidadDesdeInventario_attributesNull_retornaNull() {
            // Arrange
            InventarioResponseJsonApiDTO.Data inventarioSinAttributes = new InventarioResponseJsonApiDTO.Data();
            inventarioSinAttributes.setId("1");
            inventarioSinAttributes.setAttributes(null);

            // Act
            Integer cantidad = productosService.obtenerCantidadDesdeInventario(1L, List.of(inventarioSinAttributes));

            // Assert
            assertNull(cantidad);
        }
    }
  
}