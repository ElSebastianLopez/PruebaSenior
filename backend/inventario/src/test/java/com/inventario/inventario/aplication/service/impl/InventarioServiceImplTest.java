package com.inventario.inventario.aplication.service.impl;

import com.inventario.inventario.domain.model.Inventario;
import com.inventario.inventario.domain.repository.InventarioRepository;
import com.inventario.inventario.shared.dto.*;
import com.inventario.inventario.shared.mapper.InventarioMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private InventarioMapper inventarioMapper;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crearInventario_conDatosValidos_retornaDTO() {
        // Arrange
        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(10);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);
        inventario.setUltimaActualizacion(LocalDateTime.now());

        when(inventarioMapper.fromJsonApiDTO(request)).thenReturn(inventario);
        when(inventarioRepository.save(any())).thenReturn(inventario);

        InventarioResponseJsonApiDTO mockResponse = new InventarioResponseJsonApiDTO();
        when(inventarioMapper.toJsonApiDTO(inventario)).thenReturn(mockResponse);

        // Act
        var result = inventarioService.crearInventario(request);

        // Assert
        assertNotNull(result);
        verify(inventarioRepository).save(any());
    }

    @Test
    void crearInventario_productoIdNulo_lanzaExcepcion() {
        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(null);
        attributes.setCantidadDisponible(10);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        assertThrows(IllegalArgumentException.class, () ->
                inventarioService.crearInventario(request));
    }

    @Test
    void descontarInventario_cantidadInsuficiente_lanzaExcepcion() {
        var attributes = new InventarioDescontarRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadARestar(5);
        var request = new InventarioDescontarRequestJsonApiDTO();
        request.setData(new InventarioDescontarRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(3); // insuficiente

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        assertThrows(IllegalArgumentException.class, () ->
                inventarioService.descontarInventario(request));
    }

    @Test
    void descontarInventario_ok_retornaDTO() {
        var attributes = new InventarioDescontarRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadARestar(2);
        var request = new InventarioDescontarRequestJsonApiDTO();
        request.setData(new InventarioDescontarRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        Inventario inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidadDisponible(10);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any())).thenReturn(inventario);

        InventarioResponseJsonApiDTO mockResponse = new InventarioResponseJsonApiDTO();
        when(inventarioMapper.toJsonApiDTO(inventario)).thenReturn(mockResponse);

        var result = inventarioService.descontarInventario(request);

        assertNotNull(result);
        verify(inventarioRepository).save(any());
    }

    @Test
    void obtenerInventariosPorProductoIds_listaVacia_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                inventarioService.obtenerInventariosPorProductoIds(Collections.emptyList()));
    }

    @Test
    void actualizarInventario_cantidadDiferente_actualizaYRetornaDTO() {
        // Arrange
        Long productoId = 1L;
        Integer nuevaCantidad = 15;

        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(productoId);
        attributes.setCantidadDisponible(nuevaCantidad);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        Inventario inventario = new Inventario();
        inventario.setProductoId(productoId);
        inventario.setCantidadDisponible(10); // distinta

        when(inventarioRepository.findById(productoId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any())).thenReturn(inventario);
        when(inventarioMapper.toJsonApiDTO(inventario)).thenReturn(new InventarioResponseJsonApiDTO());

        // Act
        var result = inventarioService.actualizarInventario(request);

        // Assert
        assertNotNull(result);
        verify(inventarioRepository).save(any());
    }

    @Test
    void actualizarInventario_mismaCantidad_noActualiza() {
        Long productoId = 1L;
        Integer cantidad = 10;

        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(productoId);
        attributes.setCantidadDisponible(cantidad);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        Inventario inventario = new Inventario();
        inventario.setProductoId(productoId);
        inventario.setCantidadDisponible(10); // igual

        when(inventarioRepository.findById(productoId)).thenReturn(Optional.of(inventario));
        when(inventarioMapper.toJsonApiDTO(inventario)).thenReturn(new InventarioResponseJsonApiDTO());

        var result = inventarioService.actualizarInventario(request);

        assertNotNull(result);
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void actualizarInventario_productoIdNulo_lanzaExcepcion() {
        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(null);
        attributes.setCantidadDisponible(10);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        assertThrows(IllegalArgumentException.class, () ->
                inventarioService.actualizarInventario(request)
        );
    }

    @Test
    void actualizarInventario_cantidadInvalida_lanzaExcepcion() {
        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(-1);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        assertThrows(IllegalArgumentException.class, () ->
                inventarioService.actualizarInventario(request)
        );
    }

    @Test
    void actualizarInventario_productoNoEncontrado_lanzaExcepcion() {
        var attributes = new InventarioRequestJsonApiDTO.Data.Attributes();
        attributes.setProductoId(999L);
        attributes.setCantidadDisponible(10);
        var request = new InventarioRequestJsonApiDTO();
        request.setData(new InventarioRequestJsonApiDTO.Data());
        request.getData().setAttributes(attributes);

        when(inventarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                inventarioService.actualizarInventario(request)
        );
    }

    @Test
    void crearInventario_SinDataODatosNulos_DeberiaLanzarExcepcion() {
        InventarioRequestJsonApiDTO requestSinData = new InventarioRequestJsonApiDTO();
        // request.getData() == null

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.crearInventario(requestSinData));
        assertEquals("Los campos 'data' y 'data.attributes' son obligatorios.", ex1.getMessage());

        // Ahora test con data pero sin attributes
        InventarioRequestJsonApiDTO.Data dataSinAttributes = new InventarioRequestJsonApiDTO.Data();
        InventarioRequestJsonApiDTO requestSinAttributes = new InventarioRequestJsonApiDTO();
        requestSinAttributes.setData(dataSinAttributes);

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.crearInventario(requestSinAttributes));
        assertEquals("Los campos 'data' y 'data.attributes' son obligatorios.", ex2.getMessage());
    }

    @Test
    void crearInventario_ConCantidadInvalida_DeberiaLanzarExcepcion() {
        // Arrange
        InventarioRequestJsonApiDTO request = new InventarioRequestJsonApiDTO();
        InventarioRequestJsonApiDTO.Data data = new InventarioRequestJsonApiDTO.Data();
        InventarioRequestJsonApiDTO.Data.Attributes attributes = new InventarioRequestJsonApiDTO.Data.Attributes();

        attributes.setProductoId(1L); // válido
        attributes.setCantidadDisponible(0); // inválido

        data.setAttributes(attributes);
        request.setData(data);

        // cantidad = 0
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.crearInventario(request));
        assertEquals("La cantidad disponible debe ser mayor a cero.", ex1.getMessage());

        // cantidad = null
        attributes.setCantidadDisponible(null);
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.crearInventario(request));
        assertEquals("La cantidad disponible debe ser mayor a cero.", ex2.getMessage());
    }


}
