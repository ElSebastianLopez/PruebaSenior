package com.productos.productos.infrastructure.persistence.spec;

import com.productos.productos.domain.model.Producto;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductoSpecification {

    public static Specification<Producto> construir(ProductoFiltroDTO filtro) {
        return Specification
                .where(nombreContiene(filtro.getNombre()))
                .and(categoriaEs(filtro.getCategoria()))
                .and(precioMayorIgual(filtro.getPrecioMin()))
                .and(precioMenorIgual(filtro.getPrecioMax()));
    }

    private static Specification<Producto> nombreContiene(String nombre) {
        return (root, query, builder) ->
                nombre == null ? null : builder.like(builder.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
    }

    private static Specification<Producto> categoriaEs(String categoria) {
        return (root, query, builder) ->
                categoria == null ? null : builder.equal(root.get("categoria"), categoria);
    }

    private static Specification<Producto> precioMayorIgual(BigDecimal min) {
        return (root, query, builder) ->
                min == null ? null : builder.greaterThanOrEqualTo(root.get("precio"), min);
    }

    private static Specification<Producto> precioMenorIgual(BigDecimal max) {
        return (root, query, builder) ->
                max == null ? null : builder.lessThanOrEqualTo(root.get("precio"), max);
    }
}
