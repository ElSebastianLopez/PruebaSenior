package com.productos.productos.infrastructure.client;


import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.InventarioListResponseJsonApiDTO;
import com.productos.productos.shared.dto.InventarioRequestJsonApiDTO;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Component
public class InventarioClient {
    private final RestTemplate restTemplate;

    @Value("${inventario.api.url}")
    private String apiUrlInventario;

    @Value("${api.key}")
    private String apiKey;

    public InventarioResponseJsonApiDTO crearInventarioConReintentos(Long productoId, Integer cantidad) {
        String url = apiUrlInventario+"inventarios";
        InventarioRequestJsonApiDTO request = new InventarioRequestJsonApiDTO(productoId, cantidad);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventarioRequestJsonApiDTO> entity = new HttpEntity<>(request, headers);

        int reintentos = 0;
        Exception ultimaExcepcion = null;

        while (reintentos < 3) {
            try {
                ResponseEntity<InventarioResponseJsonApiDTO> response =
                        restTemplate.postForEntity(url, entity, InventarioResponseJsonApiDTO.class);

                // Verificar explícitamente el código de estado
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("[InventarioClient] Inventario creado correctamente para producto ID {}", productoId);
                    return response.getBody();
                } else {
                    // Manejar explícitamente otros códigos de estado
                    throw new InventarioException("Error al crear inventario. Código de estado: " + response.getStatusCodeValue(),"");
                }
            } catch (Exception e) {
                reintentos++;
                ultimaExcepcion = e;
                log.warn("[InventarioClient] Reintento {}/3 fallido al crear inventario: {}", reintentos, e.getMessage());

                // Añadir un pequeño retraso entre reintentos
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("[InventarioClient] Fallo tras 3 intentos al crear inventario para producto ID {}", productoId);
        throw new InventarioException("No se pudo crear el inventario para el producto. Último error: " +
                (ultimaExcepcion != null ? ultimaExcepcion.getMessage() : "Desconocido"),"");
    }

    public List<InventarioResponseJsonApiDTO.Data> obtenerInventariosDesdeMicroservicio(List<Long> productoIds) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", apiKey);

            HttpEntity<List<Long>> request = new HttpEntity<>(productoIds, headers);
            String url = apiUrlInventario + "inventarios/buscar";

            ResponseEntity<InventarioListResponseJsonApiDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    InventarioListResponseJsonApiDTO.class
            );

            return response.getBody().getData();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[ProductosService] Error al consumir inventario: {}", e.getMessage(), e);
            throw new InventarioException("Fallo al conectar con el inventario.", e.getResponseBodyAsString());
        }
    }

    public void actualizarInventario(Long productoId, Integer nuevaCantidad) {
        try {
            String url = apiUrlInventario + "inventarios";

            InventarioRequestJsonApiDTO request = new InventarioRequestJsonApiDTO(productoId, nuevaCantidad);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<InventarioRequestJsonApiDTO> entity = new HttpEntity<>(request, headers);

            restTemplate.put(url, entity);
            log.info("[InventarioClient] Inventario actualizado para producto ID {}", productoId);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[InventarioClient] Error al actualizar inventario: {}", e.getMessage(), e);
            throw new InventarioException("Fallo al actualizar inventario.", e.getResponseBodyAsString());
        }
    }
}
