package com.inventario.inventario.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioListaResponseJsonApiDTO {
    private List<InventarioResponseJsonApiDTO.Data> data;
}
