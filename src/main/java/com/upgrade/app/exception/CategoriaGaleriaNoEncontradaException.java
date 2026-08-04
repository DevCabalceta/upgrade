package com.upgrade.app.exception;

public class CategoriaGaleriaNoEncontradaException extends RuntimeException {
    public CategoriaGaleriaNoEncontradaException(Long id) {
        super("No existe la categoría de galería con id " + id + ".");
    }
}
