package com.productos.productos.infrastructure.rest;

import com.productos.productos.aplication.service.ProductosService;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Productos", description = "Operaciones relacionadas con productos")
public class ProductosController {
    private final ProductosService productosService;

    @Operation(
            summary = "Filtra productos paginados",
            description = "Permite obtener un listado de productos filtrados por nombre, categoría y rango de precio, con paginación.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado de productos filtrado exitosamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProductoResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @PostMapping(value = "/filtro", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductoResponseJsonApiDTO listarProductosConFiltro(
            @Valid @RequestBody ProductoFiltroDTO filtro,
            HttpServletRequest request
    ) {

        log.info("[ProductosController] Recibida solicitud de filtrado de productos");
        return productosService.getTodosLosProductos(filtro,request.getRequestURL().toString());
    }

    @Operation(
            summary = "Obtiene un producto por su ID",
            description = "Retorna un producto específico según su ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Producto encontrado",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProductoResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductoResponseJsonApiDTO obtenerProductoPorId(
            @PathVariable Long id
    ) {
        log.info("[ProductosController] Recibida solicitud de producto por ID: {}", id);
        return productosService.getProductoPorId(id);
    }

    @Operation(
            summary = "Crear un nuevo producto",
            description = "Permite registrar un producto en la base de datos",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProductoResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content),
                    @ApiResponse(responseCode = "502", description = "Error al conectarse con el inventario", content = @Content)
            }
    )
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponseJsonApiDTO crearProducto(
            @Valid @RequestBody ProductoRequestJsonApiDTO request
    ) {
        log.info("[ProductosController] Solicitud de creación de producto recibida");
        return productosService.crearProducto(request);
    }

    @Operation(
            summary = "Actualizar un producto",
            description = "Permite actualizar un producto existente por ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ProductoResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductoResponseJsonApiDTO actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestJsonApiDTO request
    ) {
        log.info("[ProductosController] Solicitud para actualizar producto ID: {}", id);
        return productosService.actualizarProducto(id, request);
    }

    @Operation(
            summary = "Eliminar un producto",
            description = "Elimina un producto existente por su ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
                    @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
                    @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id) {
        log.info("[ProductosController] Solicitud para eliminar producto ID: {}", id);
        productosService.eliminarProductoPorId(id);
    }
}
