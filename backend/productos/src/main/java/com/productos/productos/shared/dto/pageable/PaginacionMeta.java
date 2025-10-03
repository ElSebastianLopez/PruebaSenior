package com.productos.productos.shared.dto.pageable;

import lombok.Data;

@Data
public class PaginacionMeta {
    private int page;
    private int size;
    private long total;
    private int totalPages;
    private String order;
}
