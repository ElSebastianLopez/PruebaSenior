package com.productos.productos.aplication.service;

import com.productos.productos.domain.model.Producto;

public interface ProductoTransactionalService {
    Producto crearProductoYInventario(Producto producto, int cantidad);
}
