package com.productos.productos.shared.dto;

import lombok.Data;

@Data
public class InventarioRequestJsonApiDTO {
    private Data data;

    public InventarioRequestJsonApiDTO(Long productoId, Integer cantidadDisponible) {
        this.data = new Data();
        this.data.setAttributes(new Data.Attributes());
        this.data.getAttributes().setProductoId(productoId);
        this.data.getAttributes().setCantidadDisponible(cantidadDisponible);
    }

    @lombok.Data
    public static class Data {
        private String type = "inventario";
        private Attributes attributes;

        @lombok.Data
        public static class Attributes {
            private Long productoId;
            private Integer cantidadDisponible;
        }
    }
}
