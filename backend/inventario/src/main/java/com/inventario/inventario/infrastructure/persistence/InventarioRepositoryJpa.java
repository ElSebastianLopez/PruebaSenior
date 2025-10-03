package com.inventario.inventario.infrastructure.persistence;

import com.inventario.inventario.domain.model.Inventario;
import com.inventario.inventario.domain.repository.InventarioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InventarioRepositoryJpa implements InventarioRepository {
    public final  SpringDataJpaRepository springDataJpaRepository;
    public InventarioRepositoryJpa(SpringDataJpaRepository springDataJpaRepository){
        this.springDataJpaRepository = springDataJpaRepository;
    }
    @Override
    public Inventario save(Inventario inventario){
        return springDataJpaRepository.save(inventario);
    }

    @Override
    public   List<Inventario> findAllByProductoIdIn(List<Long> productoIds){
        return  springDataJpaRepository.findAllByProductoIdIn(productoIds);
    }

    @Override
    public Optional<Inventario> findById(Long id){
        return springDataJpaRepository.findById(id);
    }
}
