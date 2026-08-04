package com.upgrade.app.exception;

public class ArchivoGaleriaException extends RuntimeException {
    public ArchivoGaleriaException(String message) {
        super(message);
    }

    public ArchivoGaleriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
