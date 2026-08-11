package com.upgrade.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMovimientoInventario {
    REGISTRO("Registro"),
    ACTUALIZACION("Actualización"),
    CAMBIO_ESTADO("Cambio de estado"),
    PRESTAMO("Préstamo"),
    DEVOLUCION("Devolución"),
    BAJA("Baja");

    private final String etiqueta;
}
