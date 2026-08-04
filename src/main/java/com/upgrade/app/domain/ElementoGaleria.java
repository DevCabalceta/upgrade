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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "elemento_galeria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_elemento_galeria_titulo", columnNames = "titulo")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementoGaleria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMediaGaleria tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuenteMediaGaleria fuente;

    @Column(name = "ruta_media", nullable = false, length = 500)
    private String rutaMedia;

    @Column(name = "ruta_portada", length = 500)
    private String rutaPortada;

    @Column(nullable = false, length = 150)
    private String titulo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaGaleria categoria;

    @Column(length = 600)
    private String descripcion;

    @Column(name = "texto_alternativo", nullable = false, length = 200)
    private String textoAlternativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisenoGaleria diseno;

    @Column(nullable = false)
    @Builder.Default
    private Boolean publicado = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean destacado = false;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "nombre_archivo_original", length = 255)
    private String nombreArchivoOriginal;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = fechaCreacion == null ? ahora : fechaCreacion;
        fechaActualizacion = ahora;
        publicado = publicado == null ? true : publicado;
        destacado = destacado == null ? false : destacado;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public boolean esVideo() {
        return tipo == TipoMediaGaleria.VIDEO;
    }

    public String getRutaMiniatura() {
        return esVideo() && rutaPortada != null && !rutaPortada.isBlank() ? rutaPortada : rutaMedia;
    }
}
