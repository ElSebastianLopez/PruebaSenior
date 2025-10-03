package com.inventario.inventario.infrastructure.rest;

import com.inventario.inventario.aplication.service.InventarioService;
import com.inventario.inventario.shared.dto.InventarioDescontarRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioListaResponseJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioResponseJsonApiDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventario", description = "Operaciones sobre el inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    @Operation(
            summary = "Crear un inventario",
            description = "Permite registrar un nuevo inventario para un producto",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Inventario creado exitosamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = InventarioResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Inventario ya existe para ese producto", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InventarioResponseJsonApiDTO crearInventario(
            @Valid @RequestBody InventarioRequestJsonApiDTO request
    ) {
        log.info("[InventarioController] Solicitud de creación de inventario recibida");
        return inventarioService.crearInventario(request);
    }

    @Operation(
            summary = "Obtener inventarios por IDs de productos",
            description = "Devuelve una lista de inventarios en formato JSON:API para los productos indicados",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inventarios encontrados",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = InventarioResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud malformada", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @PostMapping(value = "/buscar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public InventarioListaResponseJsonApiDTO obtenerInventarios(@RequestBody List<Long> productoIds) {
        List<InventarioResponseJsonApiDTO.Data> inventarios = inventarioService.obtenerInventariosPorProductoIds(productoIds);
        InventarioListaResponseJsonApiDTO response = new InventarioListaResponseJsonApiDTO();
        response.setData(inventarios);
        return response;
    }

    @Operation(
            summary = "Descontar inventario de un producto",
            description = "Resta una cantidad específica del inventario de un producto",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inventario actualizado correctamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = InventarioResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Petición inválida", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Inventario no encontrado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
            }
    )
    @PostMapping(value = "/descontar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public InventarioResponseJsonApiDTO descontarInventario(
            @Valid @RequestBody InventarioDescontarRequestJsonApiDTO request) {
        log.info("[InventarioController] Solicitud para descontar inventario de producto ID: {}",
                request.getData().getAttributes().getProductoId());
        return inventarioService.descontarInventario(request);
    }

    @Operation(
            summary = "Actualizar cantidad de inventario",
            description = "Actualiza la cantidad disponible de un producto en inventario si ha cambiado",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cantidad actualizada (si fue necesario)",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = InventarioResponseJsonApiDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Inventario no encontrado", content = @Content)
            }
    )
    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public InventarioResponseJsonApiDTO actualizarInventario(
            @Valid @RequestBody InventarioRequestJsonApiDTO request) {
        log.info("[InventarioController] Solicitud de actualización recibida para inventario de producto ID: {}",
                request.getData().getAttributes().getProductoId());
        return inventarioService.actualizarInventario(request);
    }
}
