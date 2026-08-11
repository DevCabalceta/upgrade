package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoColaborador {
    DISPONIBLE("Disponible"),
    EN_EVENTO("En evento"),
    VACACIONES("Vacaciones");

    private final String etiqueta;
}
