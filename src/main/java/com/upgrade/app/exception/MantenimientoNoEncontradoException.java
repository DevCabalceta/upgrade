package com.upgrade.app.exception;

public class MantenimientoNoEncontradoException extends RuntimeException {

    public MantenimientoNoEncontradoException(Long id) {
        super("No se encontró la orden de mantenimiento con id " + id + ".");
    }
}
