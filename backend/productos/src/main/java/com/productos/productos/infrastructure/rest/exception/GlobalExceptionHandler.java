package com.productos.productos.infrastructure.rest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> manejarEntidadNoEncontrada(EntityNotFoundException ex) {
        log.warn("[ERROR 404] {}", ex.getMessage());
        return buildJsonApiErrorResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresValidacion(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errores = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> err = new HashMap<>();
            err.put("title", "Error de validación");
            err.put("detail", error.getField() + ": " + error.getDefaultMessage());
            errores.add(err);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("errors", errores);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarRuntime(RuntimeException ex) {
        log.error("[ERROR 500] {}", ex.getMessage(), ex);
        return buildJsonApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        log.warn("[ERROR 400] {}", ex.getMessage());
        return buildJsonApiErrorResponse(HttpStatus.BAD_REQUEST, "Argumento inválido", ex.getMessage());
    }

    @ExceptionHandler(InventarioException.class)
    public ResponseEntity<?> manejarErrorInventario(InventarioException ex) {
        log.error("[ERROR INVENTARIO] {}", ex.getMessage());

        try {
            // Intentar parsear la respuesta original del micro de inventario
            String cuerpoJsonOriginal = ex.getMessage().replace("Error al crear inventario: ", "").trim();

            // Retornar tal cual lo que vino del otro microservicio
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(new ObjectMapper().readValue(cuerpoJsonOriginal, Map.class));
        } catch (Exception e) {
            // Si algo falla en el parseo, regresar un JSON:API estándar de error
            return buildJsonApiErrorResponse(
                    HttpStatus.BAD_GATEWAY,
                    "Error al interactuar con Inventario",
                    ex.getMessage()
            );
        }
    }

    private ResponseEntity<Map<String, Object>> buildJsonApiErrorResponse(HttpStatus status, String title, String detail) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", String.valueOf(status.value()));
        error.put("title", title);
        error.put("detail", detail);

        Map<String, Object> response = new HashMap<>();
        response.put("errors", List.of(error));

        return new ResponseEntity<>(response, status);
    }


}
