package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisenoGaleria {
    ESTANDAR("Estándar", "lg:col-span-1 lg:row-span-1"),
    HORIZONTAL("Horizontal", "lg:col-span-2 lg:row-span-1"),
    GRANDE("Grande", "lg:col-span-2 lg:row-span-2");

    private final String etiqueta;
    private final String clasesBento;
}
