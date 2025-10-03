package com.productos.productos.aplication.service.impl;

import com.productos.productos.aplication.service.ProductoTransactionalService;
import com.productos.productos.domain.model.Producto;
import com.productos.productos.domain.repository.ProductosRepository;
import com.productos.productos.infrastructure.client.InventarioClient;
import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import com.productos.productos.shared.mapper.ProductoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoTransactionalServiceImpl implements ProductoTransactionalService {

    private final ProductosRepository productoRepository;
    private final InventarioClient inventarioClient;

    @Override
    @Transactional
    public Producto crearProductoYInventario(Producto producto, int cantidad) {
        log.info("[ProductoTransactionalService] Iniciando transacción para crear producto e inventario");

        // Primero guardamos el producto para obtener su ID
        Producto guardado = productoRepository.save(producto);
        log.info("[ProductoTransactionalService] Producto guardado con ID: {}", guardado.getId());

        try {
            // Intentamos crear el inventario
            InventarioResponseJsonApiDTO inventarioResponse =
                    inventarioClient.crearInventarioConReintentos(guardado.getId(), cantidad);

            // Verificar que la respuesta del inventario sea válida
            if (inventarioResponse == null || !isValidInventarioResponse(inventarioResponse)) {
                log.error("[ProductoTransactionalService] Respuesta de inventario no válida");
                throw new InventarioException("La respuesta del servicio de inventario no es válida","");
            }

            log.info("[ProductoTransactionalService] Producto e inventario creados correctamente");
            return guardado;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[ProductoTransactionalService] Error al crear inventario, se revertirá la transacción: {}", e.getMessage());
            throw new InventarioException("Error al crear inventario: " + e.getMessage(), e.getResponseBodyAsString());
        }
    }

    /**
     * Verifica que la respuesta del servicio de inventario sea válida según los criterios necesarios
     */
    private boolean isValidInventarioResponse(InventarioResponseJsonApiDTO response) {
        // Implementar la lógica de validación específica para tu respuesta
        // Por ejemplo:
        return response.getData() != null &&
                response.getData().getId() != null;
    }
}
