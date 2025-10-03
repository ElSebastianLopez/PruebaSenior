package com.productos.productos.shared.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequestJsonApiDTO {

    private Data data;

    @lombok.Data
    public static class Data {
        private String type;
        private Attributes attributes;
    }

    @lombok.Data
    public static class Attributes {
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
        private String categoria;
        private Integer cantidad;
    }
}
