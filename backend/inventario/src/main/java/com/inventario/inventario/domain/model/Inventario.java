package com.inventario.inventario.domain.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventarios" ,schema = "dbo")
@Data
public class Inventario {
    @Id
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidadDisponible;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}
