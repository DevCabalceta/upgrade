package com.upgrade.app.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = GaleriaController.class)
public class GaleriaUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String manejarArchivoExcedido(
            MaxUploadSizeExceededException exception,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(
                "mensajeError",
                "El archivo supera el tamaño permitido. Máximo: 10 MB para imágenes y 100 MB para videos."
        );
        return "redirect:/admin/galeria";
    }
}
