package com.productos.productos.domain.repository;

import com.productos.productos.domain.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface ProductosRepository {

    Page<Producto> findAll(Specification<Producto> specification,Pageable pageable);
    Optional<Producto> findById(Long id);
    Producto save(Producto producto);
    void delete(Producto producto);
}
