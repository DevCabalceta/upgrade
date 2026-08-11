package com.upgrade.app.exception;

public class ColaboradorNoEncontradoException extends RuntimeException {
    public ColaboradorNoEncontradoException(Long id) {
        super("No se encontró el colaborador con id " + id + ".");
    }
}
