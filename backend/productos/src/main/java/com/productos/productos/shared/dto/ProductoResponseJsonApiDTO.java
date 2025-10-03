package com.productos.productos.shared.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoResponseJsonApiDTO {
    private Links links;
    private List<Data> data;
    private Object meta;

    @lombok.Data
    public static class Data {
        private String type = "producto";
        private String id;
        private Attributes attributes;

        @lombok.Data
        public static class Attributes {
            private String nombre;
            private String descripcion;
            private BigDecimal precio;
            private String categoria;
            private Integer cantidad;
            private String creadoEn;
            private String actualizadoEn;
        }
    }

    @lombok.Data
    public static class Links {
        private String self;
        private String next;
        private String last;
    }
}
