package com.upgrade.app.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter
public class EstadoInventarioConverter implements AttributeConverter<EstadoInventario, String> {

    @Override
    public String convertToDatabaseColumn(EstadoInventario estado) {
        return estado == null ? null : estado.getEtiqueta();
    }

    @Override
    public EstadoInventario convertToEntityAttribute(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return Arrays.stream(EstadoInventario.values())
                .filter(estado -> estado.getEtiqueta().equalsIgnoreCase(valor)
                        || estado.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de inventario desconocido: " + valor));
    }
}
