package com.upgrade.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PreguntaFrecuenteNoEncontradaException extends RuntimeException {

    public PreguntaFrecuenteNoEncontradaException(Long id) {
        super("No se encontró la pregunta frecuente con id " + id + ".");
    }
}
