package com.productos.productos.shared.dto.pageable;

import lombok.Data;

@Data
public class PageableRequest {
    private int page = 0;
    private int size = 10;
    private String order = "asc";
}
