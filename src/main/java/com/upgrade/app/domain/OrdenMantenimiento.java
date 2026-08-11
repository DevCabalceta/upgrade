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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "orden_mantenimiento")
@Getter
@Setter
@NoArgsConstructor
public class OrdenMantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Usuario creadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMantenimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrioridadMantenimiento prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoMantenimiento estado = EstadoMantenimiento.PROGRAMADO;

    @Column(length = 180)
    private String ubicacion;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "hora_programada", nullable = false)
    private LocalTime horaProgramada;

    @Column(name = "costo_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoEstimado = BigDecimal.ZERO;

    @Column(name = "duracion_estimada", nullable = false, precision = 6, scale = 2)
    private BigDecimal duracionEstimada = BigDecimal.ONE;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "resumen_cierre", columnDefinition = "TEXT")
    private String resumenCierre;

    @Column(name = "horas_reales", precision = 6, scale = 2)
    private BigDecimal horasReales;

    @Column(name = "proxima_revision")
    private LocalDate proximaRevision;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_inventario_previo", length = 40)
    private EstadoInventario estadoInventarioPrevio;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = fechaCreacion == null ? ahora : fechaCreacion;
        fechaActualizacion = ahora;
        activo = activo == null ? true : activo;
        estado = estado == null ? EstadoMantenimiento.PROGRAMADO : estado;
        costoEstimado = costoEstimado == null ? BigDecimal.ZERO : costoEstimado;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public boolean isVencida() {
        return Boolean.TRUE.equals(activo)
                && estado != null
                && !estado.estaCerrado()
                && fechaProgramada != null
                && fechaProgramada.isBefore(LocalDate.now());
    }

    public String getDestinoMostrado() {
        if (cliente != null) {
            return cliente.getNombreMostrado();
        }
        return ubicacion == null || ubicacion.isBlank() ? "Bodega central" : ubicacion;
    }
}
