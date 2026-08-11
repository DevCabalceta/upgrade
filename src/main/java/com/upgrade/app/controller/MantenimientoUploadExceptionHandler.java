package com.upgrade.app.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = MantenimientoController.class)
public class MantenimientoUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String manejarArchivoExcedido(
            MaxUploadSizeExceededException exception,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(
                "mensajeError",
                "La carga supera el tamaño permitido. Cada evidencia puede pesar como máximo 20 MB."
        );
        return "redirect:/admin/mantenimiento";
    }
}
