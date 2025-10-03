package com.productos.productos.shared.mapper;

import com.productos.productos.domain.model.Producto;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponseJsonApiDTO.Data toJsonApiDTOData(Producto producto, Integer cantidadDisponible) {
        ProductoResponseJsonApiDTO.Data data = new ProductoResponseJsonApiDTO.Data();
        data.setId(String.valueOf(producto.getId()));
        data.setType("producto");

        ProductoResponseJsonApiDTO.Data.Attributes attributes = new ProductoResponseJsonApiDTO.Data.Attributes();
        attributes.setNombre(producto.getNombre());
        attributes.setDescripcion(producto.getDescripcion());
        attributes.setPrecio(producto.getPrecio());
        attributes.setCategoria(producto.getCategoria());
        attributes.setCreadoEn(producto.getCreadoEn().toString());
        attributes.setActualizadoEn(producto.getActualizadoEn().toString());
        attributes.setCantidad(cantidadDisponible);

        data.setAttributes(attributes);
        return data;
    }



    public Producto fromCreateDTO(ProductoRequestJsonApiDTO request) {
        Producto producto = new Producto();
        ProductoRequestJsonApiDTO.Attributes attr = request.getData().getAttributes();
        producto.setNombre(attr.getNombre());
        producto.setDescripcion(attr.getDescripcion());
        producto.setPrecio(attr.getPrecio());
        producto.setCategoria(attr.getCategoria());
        return producto;
    }

    public Producto updateEntityFromDTO(Producto producto, ProductoRequestJsonApiDTO request) {
        ProductoRequestJsonApiDTO.Attributes attr = request.getData().getAttributes();
        if (attr.getNombre() != null) producto.setNombre(attr.getNombre());
        if (attr.getDescripcion() != null) producto.setDescripcion(attr.getDescripcion());
        if (attr.getPrecio() != null) producto.setPrecio(attr.getPrecio());
        if (attr.getCategoria() != null) producto.setCategoria(attr.getCategoria());
        return producto;
    }
}
