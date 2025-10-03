package com.productos.productos.shared.dto.filters;

import com.productos.productos.shared.dto.pageable.PageableRequest;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class ProductoFiltroDTO {
    private String nombre;
    private String categoria;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private PageableRequest pageable = new PageableRequest();
}
