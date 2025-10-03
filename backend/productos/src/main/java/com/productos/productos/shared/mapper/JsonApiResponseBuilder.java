package com.productos.productos.shared.mapper;


import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.pageable.PaginacionMeta;
import org.springframework.data.domain.Page;

import java.util.List;

public class JsonApiResponseBuilder {
    private static final String PAGE_QUERY = "?page=";


    private JsonApiResponseBuilder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ProductoResponseJsonApiDTO build(
            Page<?> page,
            List<ProductoResponseJsonApiDTO.Data> data,
            String baseUrl,
            String sortOrder
    ) {
        ProductoResponseJsonApiDTO response = new ProductoResponseJsonApiDTO();

        // Meta
        PaginacionMeta meta = new PaginacionMeta();
        meta.setPage(page.getNumber());
        meta.setSize(page.getSize());
        meta.setTotal(page.getTotalElements());
        meta.setTotalPages(page.getTotalPages());
        meta.setOrder(sortOrder);
        response.setMeta(meta);

        // Links
        ProductoResponseJsonApiDTO.Links links = new ProductoResponseJsonApiDTO.Links();
        int currentPage = page.getNumber();
        int totalPages = page.getTotalPages();
        links.setSelf(baseUrl + PAGE_QUERY + currentPage);
        links.setNext(currentPage < totalPages - 1 ? baseUrl + PAGE_QUERY + (currentPage + 1) : null);
        links.setLast(baseUrl + PAGE_QUERY + (totalPages - 1));
        response.setLinks(links);

        // Data
        response.setData(data);

        return response;
    }
}
