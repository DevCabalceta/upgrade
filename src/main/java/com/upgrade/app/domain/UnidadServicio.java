package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnidadServicio {
    SERVICIO("Servicio"),
    HORA("Hora"),
    JORNADA("Jornada"),
    EVENTO("Evento"),
    PAQUETE("Paquete"),
    METRO_CUADRADO("m²");

    private final String etiqueta;
}
