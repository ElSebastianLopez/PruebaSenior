package com.inventario.inventario.aplication.service.impl;

import com.inventario.inventario.aplication.service.InventarioService;
import com.inventario.inventario.domain.model.Inventario;
import com.inventario.inventario.domain.repository.InventarioRepository;
import com.inventario.inventario.shared.dto.InventarioDescontarRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioResponseJsonApiDTO;
import com.inventario.inventario.shared.mapper.InventarioMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;

    @Override
    public InventarioResponseJsonApiDTO crearInventario(InventarioRequestJsonApiDTO request) {
        if (request.getData() == null || request.getData().getAttributes() == null) {
            throw new IllegalArgumentException("Los campos 'data' y 'data.attributes' son obligatorios.");
        }
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer cantidad = request.getData().getAttributes().getCantidadDisponible();

        log.info("[InventarioService] Creando inventario para producto ID: {}", productoId);

        if (productoId == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo.");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad disponible debe ser mayor a cero.");
        }

        Inventario inventario = inventarioMapper.fromJsonApiDTO(request);
        inventario.setUltimaActualizacion(LocalDateTime.now());

        Inventario guardado = inventarioRepository.save(inventario);

        return inventarioMapper.toJsonApiDTO(guardado);
    }

    @Override
    public List<InventarioResponseJsonApiDTO.Data> obtenerInventariosPorProductoIds(List<Long> productoIds) {
        if (productoIds == null || productoIds.isEmpty()) {
            throw new IllegalArgumentException("La lista de IDs de productos no puede ser vacía.");
        }

        List<Inventario> inventarios = inventarioRepository.findAllByProductoIdIn(productoIds);

        return inventarios.stream()
                .map(inventarioMapper::toJsonApiDTO)
                .map(InventarioResponseJsonApiDTO::getData)
                .toList();
    }

    @Transactional
    @Override
    public InventarioResponseJsonApiDTO descontarInventario(InventarioDescontarRequestJsonApiDTO request) {
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer cantidadARestar = request.getData().getAttributes().getCantidadARestar();

        if (productoId == null || cantidadARestar == null || cantidadARestar <= 0) {
            throw new IllegalArgumentException("Debe proporcionar un productoId válido y una cantidad a restar mayor que 0.");
        }

        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para producto ID: " + productoId));

        if (inventario.getCantidadDisponible() < cantidadARestar) {
            throw new IllegalArgumentException("No hay suficiente inventario disponible para el producto ID: " + productoId);
        }

        inventario.setCantidadDisponible(inventario.getCantidadDisponible() - cantidadARestar);
        inventarioRepository.save(inventario);

        return inventarioMapper.toJsonApiDTO(inventario);
    }

    @Override
    public InventarioResponseJsonApiDTO actualizarInventario(InventarioRequestJsonApiDTO request) {
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer nuevaCantidad = request.getData().getAttributes().getCantidadDisponible();

        if (productoId == null || nuevaCantidad == null || nuevaCantidad < 0) {
            throw new IllegalArgumentException("El ID de producto y la cantidad deben ser válidos.");
        }

        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para el producto ID: " + productoId));

        if (!inventario.getCantidadDisponible().equals(nuevaCantidad)) {
            log.info("[InventarioService] Actualizando cantidad de producto ID {} de {} a {}",
                    productoId, inventario.getCantidadDisponible(), nuevaCantidad);
            inventario.setCantidadDisponible(nuevaCantidad);
            inventarioRepository.save(inventario);
        } else {
            log.info("[InventarioService] La cantidad no ha cambiado para producto ID {}. No se realiza actualización.", productoId);
        }

        return inventarioMapper.toJsonApiDTO(inventario);
    }

}
