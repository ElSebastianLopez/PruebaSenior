package com.inventario.inventario.shared.dto;
import lombok.Data;

@Data
public class InventarioRequestJsonApiDTO {
    private Data data;

    @lombok.Data
    public static class Data {
        private String type;
        private Attributes attributes;

        @lombok.Data
        public static class Attributes {
            private Long productoId;
            private Integer cantidadDisponible;
        }
    }
}
