package com.productos.productos.infrastructure.persistence;

import com.productos.productos.domain.model.Producto;
import com.productos.productos.domain.repository.ProductosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProductosRepositoryJpa  implements ProductosRepository {
    public final  SpringDataJpaRepository springDataJpaRepository;
    public ProductosRepositoryJpa(SpringDataJpaRepository springDataJpaRepository){
        this.springDataJpaRepository = springDataJpaRepository;
    }
    @Override
    public Page<Producto> findAll(Specification<Producto> specification, Pageable pageable) {
        return springDataJpaRepository.findAll(specification, pageable);
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return springDataJpaRepository.findById(id);
    }
    @Override
    public Producto save(Producto producto) {
        return springDataJpaRepository.save(producto);
    }

    @Override
    public void delete(Producto producto) {
        springDataJpaRepository.delete(producto);
    }
}
