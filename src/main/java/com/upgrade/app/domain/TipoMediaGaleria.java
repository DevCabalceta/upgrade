package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMediaGaleria {
    IMAGEN("Fotografía"),
    VIDEO("Video");

    private final String etiqueta;
}
