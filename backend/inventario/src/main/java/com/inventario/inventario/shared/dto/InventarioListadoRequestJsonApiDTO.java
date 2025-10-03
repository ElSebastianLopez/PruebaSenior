package com.inventario.inventario.shared.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventarioListadoRequestJsonApiDTO {
    private List<Data> data;

    @lombok.Data
    public static class Data {
        private String type;
        private Long id;
    }
}
