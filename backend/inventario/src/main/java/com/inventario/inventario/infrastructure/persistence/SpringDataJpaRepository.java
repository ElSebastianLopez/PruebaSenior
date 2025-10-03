package com.inventario.inventario.infrastructure.persistence;

import com.inventario.inventario.domain.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SpringDataJpaRepository extends JpaRepository<Inventario,Long>, JpaSpecificationExecutor<Inventario> {
    List<Inventario> findAllByProductoIdIn(List<Long> productoIds);
}
