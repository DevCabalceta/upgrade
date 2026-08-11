package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoInventario {
    DISPONIBLE("Disponible"),
    EN_BODEGA("En Bodega"),
    EN_PRESTAMO("En Préstamo"),
    EN_CAMION("En Camión"),
    EN_MANTENIMIENTO("En Mantenimiento"),
    RESERVADO("Reservado"),
    DADO_DE_BAJA("Dado de baja");

    private final String etiqueta;
}
