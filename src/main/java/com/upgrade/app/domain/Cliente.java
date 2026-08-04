package com.upgrade.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente", nullable = false, length = 20)
    private TipoCliente tipoCliente = TipoCliente.EMPRESA;

    @Column(nullable = false, unique = true, length = 50)
    private String identificacion;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 150)
    private String empresa;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(length = 50)
    private String segmento;

    @Column(name = "condiciones_pago", length = 100)
    private String condicionesPago;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }

    public String getNombreMostrado() {
        return empresa != null && !empresa.isBlank() ? empresa : nombre;
    }

    public String getIniciales() {
        String base = getNombreMostrado();
        if (base == null || base.isBlank()) {
            return "CL";
        }

        String[] palabras = base.trim().split("\\s+");
        if (palabras.length == 1) {
            return palabras[0].substring(0, Math.min(2, palabras[0].length())).toUpperCase();
        }
        return (palabras[0].substring(0, 1) + palabras[1].substring(0, 1)).toUpperCase();
    }
}
