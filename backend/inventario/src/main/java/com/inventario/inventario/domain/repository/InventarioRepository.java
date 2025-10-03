package com.inventario.inventario.domain.repository;

import com.inventario.inventario.domain.model.Inventario;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository {
    Inventario save(Inventario inventario);
    List<Inventario> findAllByProductoIdIn(List<Long> productoIds);
    Optional<Inventario> findById(Long id);
}
