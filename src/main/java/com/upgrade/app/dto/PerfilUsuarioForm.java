package com.upgrade.app.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class PerfilUsuarioForm {
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Size(max = 100) private String apellido;
    @NotBlank @Email @Size(max = 100) private String email;
    @Size(max = 30) private String telefono;
    @NotNull private Long cargoId;
    @NotBlank @Pattern(regexp = "es|en") private String idioma;
    @NotBlank @Pattern(regexp = "America/Costa_Rica|America/Mexico_City|America/Bogota|America/Santiago|UTC") private String zonaHoraria;
    @Size(max = 500) private String biografia;
    private MultipartFile foto;
    private boolean eliminarFoto;

    public String getNombre() { return nombre; } public void setNombre(String v) { nombre = v; }
    public String getApellido() { return apellido; } public void setApellido(String v) { apellido = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getTelefono() { return telefono; } public void setTelefono(String v) { telefono = v; }
    public Long getCargoId() { return cargoId; } public void setCargoId(Long v) { cargoId = v; }
    public String getIdioma() { return idioma; } public void setIdioma(String v) { idioma = v; }
    public String getZonaHoraria() { return zonaHoraria; } public void setZonaHoraria(String v) { zonaHoraria = v; }
    public String getBiografia() { return biografia; } public void setBiografia(String v) { biografia = v; }
    public MultipartFile getFoto() { return foto; } public void setFoto(MultipartFile v) { foto = v; }
    public boolean isEliminarFoto() { return eliminarFoto; } public void setEliminarFoto(boolean v) { eliminarFoto = v; }
}
