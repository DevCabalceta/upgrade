package com.upgrade.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase = BigDecimal.ZERO;

    /** Se conserva por compatibilidad con el esquema original. */
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaServicio categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoServicio estado = EstadoServicio.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UnidadServicio unidad = UnidadServicio.SERVICIO;

    @Column(name = "duracion_estimada", length = 80)
    private String duracionEstimada;

    @Column(name = "equipos_requeridos", nullable = false)
    private Integer equiposRequeridos = 0;

    @Column(name = "servicios_ytd", nullable = false)
    private Integer serviciosYtd = 0;

    @Column(name = "visible_landing", nullable = false)
    private Boolean visibleLanding = true;

    @Column(nullable = false, length = 50)
    private String icono = "sparkles";

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        fechaActualizacion = ahora;
        normalizarValores();
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
        normalizarValores();
    }

    private void normalizarValores() {
        if (estado == null) {
            estado = EstadoServicio.BORRADOR;
        }
        activo = estado == EstadoServicio.ACTIVO;
        if (visibleLanding == null) {
            visibleLanding = false;
        }
        if (eliminado == null) {
            eliminado = false;
        }
        if (equiposRequeridos == null) {
            equiposRequeridos = 0;
        }
        if (serviciosYtd == null) {
            serviciosYtd = 0;
        }
        if (icono == null || icono.isBlank()) {
            icono = "sparkles";
        }
        if (orden == null) {
            orden = 0;
        }
    }

    public boolean isPublicado() {
        return estado == EstadoServicio.ACTIVO
                && Boolean.TRUE.equals(visibleLanding)
                && !Boolean.TRUE.equals(eliminado);
    }
}
