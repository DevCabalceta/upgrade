package com.upgrade.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    public String getNombreMostrado() {
        if (nombre == null || nombre.isBlank()) {
            return "Sin rol";
        }
        return switch (nombre) {
            case "ADMIN" -> "Administrador";
            case "GERENTE_OPERATIVO" -> "Gerente operativo";
            case "COORDINADOR" -> "Coordinador";
            case "TECNICO" -> "Técnico";
            case "COMERCIAL", "EJECUTIVO_VENTAS" -> "Comercial";
            case "BODEGUERO" -> "Bodeguero";
            case "AUDITOR" -> "Auditor (lectura)";
            default -> nombre.replace('_', ' ');
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
