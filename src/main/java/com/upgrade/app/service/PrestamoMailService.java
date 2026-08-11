package com.upgrade.app.service;

import com.upgrade.app.domain.Prestamo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PrestamoMailService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.mail.loan-recipient}")
    private String destinatario;

    public void enviarRegistro(Prestamo prestamo) {
        enviar(
                "[Upgrade] Nuevo préstamo " + prestamo.getFolio(),
                plantilla(
                        "Nuevo préstamo registrado",
                        "Se registró una salida de equipo en el sistema.",
                        prestamo,
                        "Estado al salir",
                        prestamo.getCondicionSalida().getEtiqueta(),
                        null
                ),
                prestamo.getCorreoContacto()
        );
    }

    public void enviarDevolucion(Prestamo prestamo) {
        enviar(
                "[Upgrade] Devolución registrada " + prestamo.getFolio(),
                plantilla(
                        "Devolución registrada",
                        "El equipo asociado al préstamo regresó al inventario.",
                        prestamo,
                        "Estado al regresar",
                        prestamo.getCondicionDevolucion().getEtiqueta(),
                        prestamo.getObservacionesDevolucion()
                ),
                prestamo.getCorreoContacto()
        );
    }

    public void enviarVencimiento(Prestamo prestamo, String mensaje) {
        enviar(
                "[Upgrade] Préstamo vencido " + prestamo.getFolio(),
                plantilla(
                        "Alerta de préstamo vencido",
                        escape(mensaje),
                        prestamo,
                        "Días de retraso",
                        prestamo.getDiasRetraso() + " día(s)",
                        prestamo.getObservaciones()
                ),
                prestamo.getCorreoContacto()
        );
    }

    private void enviar(String asunto, String html, String replyTo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(mensaje);
        } catch (MessagingException exception) {
            throw new MailPreparationException("No fue posible preparar el correo del préstamo.", exception);
        }
    }

    private String plantilla(
            String titulo,
            String introduccion,
            Prestamo prestamo,
            String etiquetaExtra,
            String valorExtra,
            String observaciones
    ) {
        return """
                <!doctype html>
                <html lang="es">
                <body style="margin:0;background:#f4f7f7;font-family:Arial,sans-serif;color:#1f2937">
                  <div style="padding:32px 16px">
                    <div style="max-width:640px;margin:auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:16px;overflow:hidden">
                      <div style="background:#111827;padding:24px 28px;color:#ffffff">
                        <div style="color:#5eead4;font-size:13px;font-weight:bold;letter-spacing:1px">UPGRADE!</div>
                        <h1 style="font-size:22px;margin:8px 0 0">%s</h1>
                      </div>
                      <div style="padding:28px">
                        <p style="margin-top:0;color:#6b7280">%s</p>
                        <table style="width:100%%;border-collapse:collapse;font-size:14px">
                          %s
                          %s
                          %s
                          %s
                          %s
                          %s
                          %s
                        </table>
                        %s
                      </div>
                      <div style="padding:16px 28px;background:#f9fafb;color:#9ca3af;font-size:12px">Notificación automática del sistema Upgrade.</div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escape(titulo),
                introduccion,
                fila("Folio", prestamo.getFolio()),
                fila("Cliente", prestamo.getCliente().getNombreMostrado()),
                fila("Equipo", prestamo.getInventario().getCodigo() + " · " + prestamo.getInventario().getNombre()),
                fila("Cantidad", String.valueOf(prestamo.getCantidad())),
                fila("Salida", FECHA.format(prestamo.getFechaSalida())),
                fila("Devolución estimada", FECHA.format(prestamo.getFechaDevolucionEstimada())),
                fila(etiquetaExtra, valorExtra),
                observaciones == null || observaciones.isBlank()
                        ? ""
                        : "<div style=\"margin-top:20px;padding:14px;background:#f3f4f6;border-radius:10px\"><b>Observaciones:</b><br>" + escape(observaciones) + "</div>"
        );
    }

    private String fila(String etiqueta, String valor) {
        return "<tr><td style=\"padding:10px;border-bottom:1px solid #e5e7eb;color:#6b7280\">"
                + escape(etiqueta)
                + "</td><td style=\"padding:10px;border-bottom:1px solid #e5e7eb;font-weight:bold\">"
                + escape(valor)
                + "</td></tr>";
    }

    private String escape(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
