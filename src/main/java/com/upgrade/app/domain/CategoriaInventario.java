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

@Entity
@Table(name = "categoria_inventario")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String icono = "package";

    @Column(nullable = false, length = 30)
    private String tono = "muted";

    @Column(nullable = false)
    private Boolean activa = true;

    @PrePersist
    void prePersist() {
        if (icono == null || icono.isBlank()) {
            icono = "package";
        }
        if (tono == null || tono.isBlank()) {
            tono = "muted";
        }
        if (activa == null) {
            activa = true;
        }
    }
}
