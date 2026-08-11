package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoMantenimiento {
    PROGRAMADO("Programado"),
    EN_PROCESO("En proceso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    public boolean estaCerrado() {
        return this == FINALIZADO || this == CANCELADO;
    }
}
