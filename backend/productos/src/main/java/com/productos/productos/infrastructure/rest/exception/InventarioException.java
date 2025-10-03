package com.productos.productos.infrastructure.rest.exception;

public class InventarioException extends RuntimeException {
    private final String rawJson;

    public InventarioException(String message, String rawJson) {
        super(message);
        this.rawJson = rawJson;
    }

    public String getRawJson() {
        return rawJson;
    }
}
