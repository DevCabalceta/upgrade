package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoServicio {
    ACTIVO("Activo"),
    BORRADOR("Borrador"),
    INACTIVO("Inactivo");

    private final String etiqueta;
}
