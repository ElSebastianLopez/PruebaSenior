package com.inventario.inventario.shared.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventarioResponseJsonApiDTO {
    private Data data;

    @lombok.Data
    public static class Data {
        private String type = "inventario";
        private String id;
        private Attributes attributes;

        @lombok.Data
        public static class Attributes {
            private Long productoId;
            private Integer cantidadDisponible;
            private LocalDateTime ultimaActualizacion;
        }
    }
}
