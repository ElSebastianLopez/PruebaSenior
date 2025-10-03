package com.productos.productos.infrastructure.persistence;

import com.productos.productos.domain.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataJpaRepository  extends JpaRepository<Producto,Long>, JpaSpecificationExecutor<Producto> {

}
