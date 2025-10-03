package com.inventario.inventario.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventario.inventario.aplication.service.InventarioService;
import com.inventario.inventario.shared.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
 class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private static final String BASE_URL = "/api/v1/inventarios";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void crearInventario_DeberiaRetornarStatus201() throws Exception {
        // Arrange
        InventarioRequestJsonApiDTO request = crearInventarioRequestMock();
        InventarioResponseJsonApiDTO response = crearInventarioResponseMock();

        when(inventarioService.crearInventario(any(InventarioRequestJsonApiDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value("1"));

        verify(inventarioService, times(1)).crearInventario(any(InventarioRequestJsonApiDTO.class));
    }

    @Test
    void obtenerInventarios_DeberiaRetornarListaDeInventarios() throws Exception {
        // Arrange
        List<Long> productoIds = Arrays.asList(1L, 2L);
        List<InventarioResponseJsonApiDTO.Data> inventarios = Arrays.asList(
                new InventarioResponseJsonApiDTO.Data(),
                new InventarioResponseJsonApiDTO.Data()
        );

        when(inventarioService.obtenerInventariosPorProductoIds(productoIds)).thenReturn(inventarios);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoIds)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray());

        verify(inventarioService, times(1)).obtenerInventariosPorProductoIds(productoIds);
    }

    @Test
    void descontarInventario_DeberiaRetornarInventarioActualizado() throws Exception {
        // Arrange
        InventarioDescontarRequestJsonApiDTO request = crearInventarioDescontarRequestMock();
        InventarioResponseJsonApiDTO response = crearInventarioResponseMock();

        when(inventarioService.descontarInventario(any(InventarioDescontarRequestJsonApiDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/descontar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1));

        verify(inventarioService, times(1)).descontarInventario(any(InventarioDescontarRequestJsonApiDTO.class));
    }

    @Test
    void actualizarInventario_DeberiaRetornarInventarioActualizado() throws Exception {
        // Arrange
        InventarioRequestJsonApiDTO request = crearInventarioRequestMock();
        InventarioResponseJsonApiDTO response = crearInventarioResponseMock();

        when(inventarioService.actualizarInventario(any(InventarioRequestJsonApiDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(inventarioService, times(1)).actualizarInventario(any(InventarioRequestJsonApiDTO.class));
    }

    @Test
    void crearInventario_ConDatosInvalidos_DeberiaRetornarStatus400() throws Exception {
        // Arrange

        // Act & Assert
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any());
    }

    @Test
    void descontarInventario_ConDatosInvalidos_DeberiaRetornarStatus400() throws Exception {

        // Act & Assert
        mockMvc.perform(post(BASE_URL + "/descontar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).descontarInventario(any());
    }


    @Test
    void obtenerInventarios_ConListaVacia_DeberiaRetornarStatus200() throws Exception {

        List<Long> productoIds = Arrays.asList();
        List<InventarioResponseJsonApiDTO.Data> inventariosVacios = Arrays.asList();

        when(inventarioService.obtenerInventariosPorProductoIds(productoIds)).thenReturn(inventariosVacios);


        mockMvc.perform(post(BASE_URL + "/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoIds)))
                .andExpect(status().isOk());

        verify(inventarioService, times(1)).obtenerInventariosPorProductoIds(eq(productoIds));
    }


    private InventarioRequestJsonApiDTO crearInventarioRequestMock() {
        InventarioRequestJsonApiDTO request = new InventarioRequestJsonApiDTO();
        InventarioRequestJsonApiDTO.Data data = new InventarioRequestJsonApiDTO.Data();
        InventarioRequestJsonApiDTO.Data.Attributes attributes = new InventarioRequestJsonApiDTO.Data.Attributes();

        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(100);
        data.setAttributes(attributes);
        request.setData(data);

        return request;
    }

    private InventarioResponseJsonApiDTO crearInventarioResponseMock() {
        InventarioResponseJsonApiDTO response = new InventarioResponseJsonApiDTO();
        InventarioResponseJsonApiDTO.Data data = new InventarioResponseJsonApiDTO.Data();
        InventarioResponseJsonApiDTO.Data.Attributes attributes = new InventarioResponseJsonApiDTO.Data.Attributes();

        attributes.setProductoId(1L);
        attributes.setProductoId(1L);
        attributes.setCantidadDisponible(100);
        data.setId("1");
        data.setType("inventario");
        data.setAttributes(attributes);
        response.setData(data);

        return response;
    }

    private InventarioDescontarRequestJsonApiDTO crearInventarioDescontarRequestMock() {
        InventarioDescontarRequestJsonApiDTO request = new InventarioDescontarRequestJsonApiDTO();
        InventarioDescontarRequestJsonApiDTO.Data data = new InventarioDescontarRequestJsonApiDTO.Data();
        InventarioDescontarRequestJsonApiDTO.Data.Attributes attributes = new InventarioDescontarRequestJsonApiDTO.Data.Attributes();

        attributes.setProductoId(1L);
        attributes.setCantidadARestar(10);
        data.setAttributes(attributes);
        request.setData(data);

        return request;
    }
}
