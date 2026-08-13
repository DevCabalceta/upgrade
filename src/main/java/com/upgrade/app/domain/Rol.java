package com.upgrade.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Column(name = "nombre_mostrado", nullable = false, length = 100)
    private String nombreMostrado;

    @Column(nullable = false)
    private Boolean protegido = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_modulo",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "modulo_id")
    )
    @OrderBy("orden ASC")
    private Set<Modulo> modulos = new LinkedHashSet<>();

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (nombreMostrado == null || nombreMostrado.isBlank()) nombreMostrado = formatearNombre(nombre);
        if (activo == null) activo = true;
        if (protegido == null) protegido = false;
    }

    public String getNombreMostrado() {
        if (nombre == null || nombre.isBlank()) {
            return "Sin rol";
        }
        if (nombreMostrado != null && !nombreMostrado.isBlank()) return nombreMostrado;
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

    public void setNombreMostrado(String nombreMostrado) { this.nombreMostrado = nombreMostrado; }
    public Boolean getProtegido() { return protegido; }
    public void setProtegido(Boolean protegido) { this.protegido = protegido; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Set<Modulo> getModulos() { return modulos; }
    public void setModulos(Set<Modulo> modulos) { this.modulos = modulos == null ? new LinkedHashSet<>() : modulos; }

    private String formatearNombre(String valor) {
        if (valor == null || valor.isBlank()) return "Sin rol";
        String limpio = valor.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(limpio.charAt(0)) + limpio.substring(1);
    }
}
