package com.inventario.inventario.aplication.service;

import com.inventario.inventario.shared.dto.InventarioDescontarRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioResponseJsonApiDTO;

import java.util.List;

public interface InventarioService {
    InventarioResponseJsonApiDTO crearInventario(InventarioRequestJsonApiDTO request);
    List<InventarioResponseJsonApiDTO.Data> obtenerInventariosPorProductoIds(List<Long> productoIds);
    InventarioResponseJsonApiDTO descontarInventario(InventarioDescontarRequestJsonApiDTO request);
    InventarioResponseJsonApiDTO actualizarInventario(InventarioRequestJsonApiDTO request);
}
