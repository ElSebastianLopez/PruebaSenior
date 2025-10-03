package com.productos.productos.aplication.service;

import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;

public interface ProductosService {
    ProductoResponseJsonApiDTO getTodosLosProductos(ProductoFiltroDTO filtro,String baseUrl);
    ProductoResponseJsonApiDTO getProductoPorId(Long id);
    ProductoResponseJsonApiDTO crearProducto(ProductoRequestJsonApiDTO request);
    ProductoResponseJsonApiDTO actualizarProducto(Long id, ProductoRequestJsonApiDTO request);
    void eliminarProductoPorId(Long id);
}
