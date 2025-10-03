package com.productos.productos.shared.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventarioListResponseJsonApiDTO {
    private List<InventarioResponseJsonApiDTO.Data> data;
}
