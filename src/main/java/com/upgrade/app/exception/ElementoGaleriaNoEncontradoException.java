package com.upgrade.app.exception;

public class ElementoGaleriaNoEncontradoException extends RuntimeException {
    public ElementoGaleriaNoEncontradoException(Long id) {
        super("No existe el elemento de galería con id " + id + ".");
    }
}
