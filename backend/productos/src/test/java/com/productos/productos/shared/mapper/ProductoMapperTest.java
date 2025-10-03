package com.productos.productos.shared.mapper;

import static org.junit.jupiter.api.Assertions.*;
import com.productos.productos.domain.model.Producto;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@DisplayName("ProductoMapper - Tests Unitarios")
class ProductoMapperTest {
    private ProductoMapper mapper;
    private Producto producto;
    private ProductoRequestJsonApiDTO requestDTO;

    @BeforeEach
    void setUp() {
        mapper = new ProductoMapper();

        // Setup Producto
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setDescripcion("Laptop gaming");
        producto.setPrecio(new BigDecimal("1500.00"));
        producto.setCategoria("Electrónica");
        producto.setCreadoEn(LocalDateTime.of(2024, 1, 1, 10, 0));
        producto.setActualizadoEn(LocalDateTime.of(2024, 1, 15, 14, 30));

        // Setup Request DTO
        ProductoRequestJsonApiDTO.Attributes attributes = new ProductoRequestJsonApiDTO.Attributes();
        attributes.setNombre("Mouse");
        attributes.setDescripcion("Mouse inalámbrico");
        attributes.setPrecio(new BigDecimal("50.00"));
        attributes.setCategoria("Accesorios");
        attributes.setCantidad(100);

        ProductoRequestJsonApiDTO.Data data = new ProductoRequestJsonApiDTO.Data();
        data.setType("productos");
        data.setAttributes(attributes);

        requestDTO = new ProductoRequestJsonApiDTO();
        requestDTO.setData(data);
    }

    @Nested
    @DisplayName("Tests de toJsonApiDTOData")
    class ToJsonApiDTODataTests {

        @Test
        @DisplayName("Debe convertir Producto a Data DTO correctamente con cantidad")
        void toJsonApiDTOData_productoConCantidad_convierteCorrectamente() {
            // Act
            ProductoResponseJsonApiDTO.Data result = mapper.toJsonApiDTOData(producto, 10);

            // Assert
            assertNotNull(result);
            assertEquals("1", result.getId());
            assertEquals("producto", result.getType());

            ProductoResponseJsonApiDTO.Data.Attributes attributes = result.getAttributes();
            assertNotNull(attributes);
            assertEquals("Laptop", attributes.getNombre());
            assertEquals("Laptop gaming", attributes.getDescripcion());
            assertEquals(new BigDecimal("1500.00"), attributes.getPrecio());
            assertEquals("Electrónica", attributes.getCategoria());
            assertEquals(10, attributes.getCantidad());
            assertEquals("2024-01-01T10:00", attributes.getCreadoEn());
            assertEquals("2024-01-15T14:30", attributes.getActualizadoEn());
        }

        @Test
        @DisplayName("Debe convertir Producto con cantidad null")
        void toJsonApiDTOData_productoSinCantidad_convierteCorrectamente() {
            // Act
            ProductoResponseJsonApiDTO.Data result = mapper.toJsonApiDTOData(producto, null);

            // Assert
            assertNotNull(result);
            assertEquals("1", result.getId());
            assertNull(result.getAttributes().getCantidad());
        }

        @Test
        @DisplayName("Debe convertir Producto con cantidad cero")
        void toJsonApiDTOData_productoConCantidadCero_convierteCorrectamente() {
            // Act
            ProductoResponseJsonApiDTO.Data result = mapper.toJsonApiDTOData(producto, 0);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getAttributes().getCantidad());
        }

        @Test
        @DisplayName("Debe manejar descripción null")
        void toJsonApiDTOData_descripcionNull_convierteCorrectamente() {
            // Arrange
            producto.setDescripcion(null);

            // Act
            ProductoResponseJsonApiDTO.Data result = mapper.toJsonApiDTOData(producto, 5);

            // Assert
            assertNotNull(result);
            assertNull(result.getAttributes().getDescripcion());
            assertEquals("Laptop", result.getAttributes().getNombre());
        }

        @Test
        @DisplayName("Debe manejar categoría null")
        void toJsonApiDTOData_categoriaNull_convierteCorrectamente() {
            // Arrange
            producto.setCategoria(null);

            // Act
            ProductoResponseJsonApiDTO.Data result = mapper.toJsonApiDTOData(producto, 5);

            // Assert
            assertNotNull(result);
            assertNull(result.getAttributes().getCategoria());
        }
    }

    @Nested
    @DisplayName("Tests de fromCreateDTO")
    class FromCreateDTOTests {

        @Test
        @DisplayName("Debe crear Producto desde Request DTO correctamente")
        void fromCreateDTO_dtoValido_creaProductoCorrectamente() {
            // Act
            Producto result = mapper.fromCreateDTO(requestDTO);

            // Assert
            assertNotNull(result);
            assertNull(result.getId()); // ID no debe establecerse en creación
            assertEquals("Mouse", result.getNombre());
            assertEquals("Mouse inalámbrico", result.getDescripcion());
            assertEquals(new BigDecimal("50.00"), result.getPrecio());
            assertEquals("Accesorios", result.getCategoria());
        }

        @Test
        @DisplayName("Debe crear Producto con descripción null")
        void fromCreateDTO_descripcionNull_creaProducto() {
            // Arrange
            requestDTO.getData().getAttributes().setDescripcion(null);

            // Act
            Producto result = mapper.fromCreateDTO(requestDTO);

            // Assert
            assertNotNull(result);
            assertNull(result.getDescripcion());
            assertEquals("Mouse", result.getNombre());
        }

        @Test
        @DisplayName("Debe crear Producto con categoría null")
        void fromCreateDTO_categoriaNull_creaProducto() {
            // Arrange
            requestDTO.getData().getAttributes().setCategoria(null);

            // Act
            Producto result = mapper.fromCreateDTO(requestDTO);

            // Assert
            assertNotNull(result);
            assertNull(result.getCategoria());
            assertEquals("Mouse", result.getNombre());
        }

        @Test
        @DisplayName("Debe crear Producto con todos los campos obligatorios")
        void fromCreateDTO_todosLosCampos_creaProductoCompleto() {
            // Act
            Producto result = mapper.fromCreateDTO(requestDTO);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getNombre());
            assertNotNull(result.getPrecio());
            // Cantidad no se mapea en fromCreateDTO
        }
    }

    @Nested
    @DisplayName("Tests de updateEntityFromDTO")
    class UpdateEntityFromDTOTests {

        @Test
        @DisplayName("Debe actualizar todos los campos del Producto")
        void updateEntityFromDTO_todosLosCampos_actualizaCorrectamente() {
            // Arrange
            Producto existente = new Producto();
            existente.setId(99L);
            existente.setNombre("Producto Viejo");
            existente.setDescripcion("Descripción vieja");
            existente.setPrecio(new BigDecimal("100.00"));
            existente.setCategoria("Categoría vieja");

            // Act
            Producto result = mapper.updateEntityFromDTO(existente, requestDTO);

            // Assert
            assertNotNull(result);
            assertEquals(99L, result.getId()); // ID no debe cambiar
            assertEquals("Mouse", result.getNombre());
            assertEquals("Mouse inalámbrico", result.getDescripcion());
            assertEquals(new BigDecimal("50.00"), result.getPrecio());
            assertEquals("Accesorios", result.getCategoria());
        }

        @Test
        @DisplayName("Debe mantener valores existentes cuando campos DTO son null")
        void updateEntityFromDTO_camposNull_mantienValoresExistentes() {
            // Arrange
            Producto existente = new Producto();
            existente.setId(99L);
            existente.setNombre("Producto Existente");
            existente.setDescripcion("Descripción existente");
            existente.setPrecio(new BigDecimal("200.00"));
            existente.setCategoria("Categoría existente");

            requestDTO.getData().getAttributes().setNombre(null);
            requestDTO.getData().getAttributes().setDescripcion(null);
            requestDTO.getData().getAttributes().setPrecio(null);
            requestDTO.getData().getAttributes().setCategoria(null);

            // Act
            Producto result = mapper.updateEntityFromDTO(existente, requestDTO);

            // Assert
            assertNotNull(result);
            assertEquals("Producto Existente", result.getNombre());
            assertEquals("Descripción existente", result.getDescripcion());
            assertEquals(new BigDecimal("200.00"), result.getPrecio());
            assertEquals("Categoría existente", result.getCategoria());
        }

        @Test
        @DisplayName("Debe actualizar solo el nombre cuando otros campos son null")
        void updateEntityFromDTO_soloNombre_actualizaSoloNombre() {
            // Arrange
            Producto existente = new Producto();
            existente.setId(99L);
            existente.setNombre("Viejo");
            existente.setDescripcion("Desc Vieja");
            existente.setPrecio(new BigDecimal("100.00"));
            existente.setCategoria("Cat Vieja");

            requestDTO.getData().getAttributes().setNombre("Nuevo Nombre");
            requestDTO.getData().getAttributes().setDescripcion(null);
            requestDTO.getData().getAttributes().setPrecio(null);
            requestDTO.getData().getAttributes().setCategoria(null);

            // Act
            Producto result = mapper.updateEntityFromDTO(existente, requestDTO);

            // Assert
            assertEquals("Nuevo Nombre", result.getNombre());
            assertEquals("Desc Vieja", result.getDescripcion());
            assertEquals(new BigDecimal("100.00"), result.getPrecio());
            assertEquals("Cat Vieja", result.getCategoria());
        }

        @Test
        @DisplayName("Debe actualizar solo el precio cuando otros campos son null")
        void updateEntityFromDTO_soloPrecio_actualizaSoloPrecio() {
            // Arrange
            Producto existente = new Producto();
            existente.setId(99L);
            existente.setNombre("Nombre");
            existente.setDescripcion("Descripción");
            existente.setPrecio(new BigDecimal("100.00"));
            existente.setCategoria("Categoría");

            requestDTO.getData().getAttributes().setNombre(null);
            requestDTO.getData().getAttributes().setDescripcion(null);
            requestDTO.getData().getAttributes().setPrecio(new BigDecimal("999.99"));
            requestDTO.getData().getAttributes().setCategoria(null);

            // Act
            Producto result = mapper.updateEntityFromDTO(existente, requestDTO);

            // Assert
            assertEquals("Nombre", result.getNombre());
            assertEquals(new BigDecimal("999.99"), result.getPrecio());
            assertEquals("Categoría", result.getCategoria());
        }

        @Test
        @DisplayName("Debe retornar el mismo objeto Producto actualizado")
        void updateEntityFromDTO_retornaMismoObjeto() {
            // Arrange
            Producto existente = new Producto();
            existente.setId(99L);

            // Act
            Producto result = mapper.updateEntityFromDTO(existente, requestDTO);

            // Assert
            assertSame(existente, result);
        }

        @Test
        @DisplayName("Debe permitir actualizar campos a valores válidos diferentes")
        void updateEntityFromDTO_valoresNuevos_actualizaCorrectamente() {
            // Arrange
            Producto existente = new Producto();
            existente.setId(1L);
            existente.setNombre("A");
            existente.setDescripcion("B");
            existente.setPrecio(new BigDecimal("1.00"));
            existente.setCategoria("C");

            // Act
            Producto result = mapper.updateEntityFromDTO(existente, requestDTO);

            // Assert
            assertEquals("Mouse", result.getNombre());
            assertEquals("Mouse inalámbrico", result.getDescripcion());
            assertEquals(new BigDecimal("50.00"), result.getPrecio());
            assertEquals("Accesorios", result.getCategoria());
        }
    }

}