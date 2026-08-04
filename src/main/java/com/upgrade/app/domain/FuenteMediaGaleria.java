package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FuenteMediaGaleria {
    ARCHIVO("Archivo local"),
    URL("URL externa");

    private final String etiqueta;
}
