package com.upgrade.app.exception;

public class InventarioNoEncontradoException extends RuntimeException {

    public InventarioNoEncontradoException(Long id) {
        super("No se encontró el equipo de inventario con id " + id + ".");
    }
}
