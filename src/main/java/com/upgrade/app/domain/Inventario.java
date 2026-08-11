package com.upgrade.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "inventario")
@Getter
@Setter
@NoArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "numero_serie", unique = true, length = 100)
    private String numeroSerie;

    @Column(length = 100)
    private String marca;

    @Column(length = 100)
    private String modelo;

    @Column(length = 100)
    private String bodega;

    @Column(length = 150)
    private String ubicacion;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @Column(name = "precio_venta", precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "cantidad_total", nullable = false)
    private Integer cantidadTotal = 1;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible = 1;

    @Convert(converter = EstadoInventarioConverter.class)
    @Column(nullable = false, length = 50)
    private EstadoInventario estado = EstadoInventario.EN_BODEGA;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaInventario categoria;

    @Column(nullable = false)
    private Boolean activo = true;

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
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public String getUbicacionCompleta() {
        if (bodega == null || bodega.isBlank()) {
            return ubicacion == null || ubicacion.isBlank() ? "Sin asignar" : ubicacion;
        }
        if (ubicacion == null || ubicacion.isBlank()) {
            return bodega;
        }
        return bodega + " · " + ubicacion;
    }
}
