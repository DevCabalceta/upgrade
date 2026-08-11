package com.upgrade.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @PrePersist
    protected void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }

    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "SV";
        }
        String[] palabras = nombre.trim().split("\\s+");
        if (palabras.length == 1) {
            return palabras[0].substring(0, Math.min(2, palabras[0].length())).toUpperCase();
        }
        return (palabras[0].substring(0, 1) + palabras[1].substring(0, 1)).toUpperCase();
    }
}
