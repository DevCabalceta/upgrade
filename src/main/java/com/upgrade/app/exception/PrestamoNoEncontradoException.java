package com.upgrade.app.exception;

public class PrestamoNoEncontradoException extends RuntimeException {

    public PrestamoNoEncontradoException(Long id) {
        super("No se encontró el préstamo con id " + id + ".");
    }
}
