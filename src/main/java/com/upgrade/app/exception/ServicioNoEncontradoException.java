package com.upgrade.app.exception;

public class ServicioNoEncontradoException extends RuntimeException {

    public ServicioNoEncontradoException(Long id) {
        super("No se encontró el servicio con id " + id + ".");
    }
}
