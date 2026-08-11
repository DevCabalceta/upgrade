package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMantenimiento {
    PREVENTIVO("Preventivo"),
    CORRECTIVO("Correctivo"),
    PREDICTIVO("Predictivo"),
    EMERGENCIA("Emergencia");

    private final String etiqueta;
}
