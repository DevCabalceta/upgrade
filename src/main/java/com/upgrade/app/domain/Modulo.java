package com.upgrade.app.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "modulo")
public class Modulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "ruta_base", nullable = false, length = 150)
    private String rutaBase;

    @Column(length = 100)
    private String icono;

    @Column(nullable = false, length = 60)
    private String grupo = "General";

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private Boolean activo = true;

    public String getAutoridad() { return "MODULO_" + nombre; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getRutaBase() { return rutaBase; }
    public void setRutaBase(String rutaBase) { this.rutaBase = rutaBase; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
