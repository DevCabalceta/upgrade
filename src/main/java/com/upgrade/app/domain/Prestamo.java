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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "prestamo")
@Getter
@Setter
@NoArgsConstructor
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Column(name = "correo_contacto", nullable = false, length = 120)
    private String correoContacto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "fecha_devolucion_estimada", nullable = false)
    private LocalDate fechaDevolucionEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_salida", nullable = false, length = 40)
    private CondicionEquipo condicionSalida;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_devolucion", length = 40)
    private CondicionEquipo condicionDevolucion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "observaciones_devolucion", columnDefinition = "TEXT")
    private String observacionesDevolucion;

    @Column(name = "ultima_notificacion_vencimiento")
    private LocalDateTime ultimaNotificacionVencimiento;

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
        if (estado == null) {
            estado = EstadoPrestamo.ACTIVO;
        }
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public boolean isVencido() {
        return estado == EstadoPrestamo.ACTIVO
                && fechaDevolucionEstimada != null
                && fechaDevolucionEstimada.isBefore(LocalDate.now());
    }

    public String getEstadoMostrado() {
        return isVencido() ? "Vencido" : estado.getEtiqueta();
    }

    public long getDiasRetraso() {
        return isVencido() ? ChronoUnit.DAYS.between(fechaDevolucionEstimada, LocalDate.now()) : 0;
    }
}
