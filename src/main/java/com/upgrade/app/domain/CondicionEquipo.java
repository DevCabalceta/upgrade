package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CondicionEquipo {
    OPTIMO("Óptimo"),
    CON_DETALLES("Con detalles"),
    RECIEN_REPARADO("Recién reparado"),
    REQUIERE_MANTENIMIENTO("Requiere mantenimiento"),
    DANADO("Dañado");

    private final String etiqueta;

    public boolean requiereMantenimiento() {
        return this == REQUIERE_MANTENIMIENTO || this == DANADO;
    }
}
