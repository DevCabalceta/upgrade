package com.upgrade.app.domain;

public enum TipoCliente {
    PERSONA("Persona"),
    EMPRESA("Empresa");

    private final String etiqueta;

    TipoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
