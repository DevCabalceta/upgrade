package com.upgrade.app.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_usuario")
public class ConfiguracionUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 10)
    private String idioma = "es";

    @Column(nullable = false, length = 50)
    private String tema = "system";

    // Se conserva porque ya existe en el esquema, aunque no se expone en la interfaz.
    @Column(nullable = false)
    private Boolean notificaciones = true;

    @Column(name = "zona_horaria", nullable = false, length = 60)
    private String zonaHoraria = "America/Costa_Rica";

    @Column(nullable = false, length = 20)
    private String densidad = "comfortable";

    @Column(length = 500)
    private String biografia;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    @Column(name = "fecha_cambio_password")
    private LocalDateTime fechaCambioPassword;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    void actualizarFecha() {
        fechaActualizacion = LocalDateTime.now();
        if (idioma == null) idioma = "es";
        if (tema == null) tema = "system";
        if (densidad == null) densidad = "comfortable";
        if (zonaHoraria == null) zonaHoraria = "America/Costa_Rica";
        if (notificaciones == null) notificaciones = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
    public Boolean getNotificaciones() { return notificaciones; }
    public void setNotificaciones(Boolean notificaciones) { this.notificaciones = notificaciones; }
    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String zonaHoraria) { this.zonaHoraria = zonaHoraria; }
    public String getDensidad() { return densidad; }
    public void setDensidad(String densidad) { this.densidad = densidad; }
    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }
    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    public LocalDateTime getFechaCambioPassword() { return fechaCambioPassword; }
    public void setFechaCambioPassword(LocalDateTime fechaCambioPassword) { this.fechaCambioPassword = fechaCambioPassword; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
