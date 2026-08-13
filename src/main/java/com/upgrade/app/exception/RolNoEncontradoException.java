package com.upgrade.app.exception;

public class RolNoEncontradoException extends RuntimeException {
    public RolNoEncontradoException(Long id) {
        super("No se encontró el rol solicitado (id " + id + ").");
    }
}
