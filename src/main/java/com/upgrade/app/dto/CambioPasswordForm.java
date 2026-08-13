package com.upgrade.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CambioPasswordForm {
    @NotBlank private String passwordActual;
    @NotBlank @Size(min = 12, max = 100) private String passwordNueva;
    @NotBlank private String confirmarPassword;
    public String getPasswordActual() { return passwordActual; } public void setPasswordActual(String v) { passwordActual = v; }
    public String getPasswordNueva() { return passwordNueva; } public void setPasswordNueva(String v) { passwordNueva = v; }
    public String getConfirmarPassword() { return confirmarPassword; } public void setConfirmarPassword(String v) { confirmarPassword = v; }
}
