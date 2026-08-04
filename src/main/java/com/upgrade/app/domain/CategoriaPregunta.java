package com.upgrade.app.domain;

public enum CategoriaPregunta {
    SERVICIOS("Servicios"),
    EQUIPOS("Equipos"),
    OPERACION("Operación"),
    CONTRATACION("Contratación"),
    LOGISTICA("Logística"),
    GENERAL("General");

    private final String etiqueta;

    CategoriaPregunta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
