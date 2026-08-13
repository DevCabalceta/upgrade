package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AparienciaForm {
    @NotBlank @Pattern(regexp = "light|dark|system") private String tema;
    @NotBlank @Pattern(regexp = "compact|comfortable|spacious") private String densidad;
    public String getTema() { return tema; } public void setTema(String v) { tema = v; }
    public String getDensidad() { return densidad; } public void setDensidad(String v) { densidad = v; }
}
