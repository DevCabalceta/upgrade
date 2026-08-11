package com.upgrade.app.exception;

public class ArchivoMantenimientoException extends RuntimeException {

    public ArchivoMantenimientoException(String message) {
        super(message);
    }

    public ArchivoMantenimientoException(String message, Throwable cause) {
        super(message, cause);
    }
}
