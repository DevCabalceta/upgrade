package com.upgrade.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ServicioNoEncontradoException extends RuntimeException {

    public ServicioNoEncontradoException(Long id) {
        super("No se encontró el servicio con id " + id + ".");
    }
}
