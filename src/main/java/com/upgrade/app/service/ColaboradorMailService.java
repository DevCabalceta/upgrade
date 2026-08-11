package com.upgrade.app.service;

import com.upgrade.app.domain.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ColaboradorMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.mail.collaborator-recipient}")
    private String destinatario;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void enviarInvitacion(Usuario colaborador) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setReplyTo(colaborador.getEmail());
            helper.setSubject("[Upgrade] Invitación para " + colaborador.getNombreCompleto());
            helper.setText(plantilla(colaborador), true);
            mailSender.send(mensaje);
        } catch (MessagingException exception) {
            throw new MailPreparationException("No fue posible preparar la invitación del colaborador.", exception);
        }
    }

    private String plantilla(Usuario colaborador) {
        return """
                <!doctype html>
                <html lang="es">
                <body style="margin:0;background:#f4f7f7;font-family:Arial,sans-serif;color:#1f2937">
                  <div style="padding:32px 16px">
                    <div style="max-width:640px;margin:auto;overflow:hidden;border:1px solid #e5e7eb;border-radius:18px;background:#fff">
                      <div style="padding:26px 30px;background:#111827;color:#fff">
                        <div style="font-size:13px;font-weight:bold;letter-spacing:1.5px;color:#5eead4">UPGRADE!</div>
                        <h1 style="margin:8px 0 0;font-size:24px">Tu acceso al sistema está listo</h1>
                      </div>
                      <div style="padding:30px">
                        <p>Hola <strong>%s</strong>,</p>
                        <p style="line-height:1.6;color:#6b7280">Se creó tu cuenta como <strong>%s</strong>. Ya puedes ingresar con cualquiera de los siguientes datos:</p>
                        <div style="margin:22px 0;padding:18px;border-radius:12px;background:#f3f4f6">
                          <div style="margin-bottom:8px"><span style="color:#6b7280">Usuario:</span> <strong>%s</strong></div>
                          <div><span style="color:#6b7280">Correo:</span> <strong>%s</strong></div>
                        </div>
                        <p style="font-size:13px;line-height:1.5;color:#6b7280">Por seguridad, la contraseña inicial no se incluye en este correo. Solicítala al administrador que creó la cuenta.</p>
                        <a href="%s/login" style="display:inline-block;margin-top:10px;padding:12px 20px;border-radius:9px;background:#45d4b4;color:#111827;text-decoration:none;font-weight:bold">Ingresar a Upgrade</a>
                      </div>
                      <div style="padding:16px 30px;background:#f9fafb;color:#9ca3af;font-size:12px">Notificación automática del sistema Upgrade.</div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escape(colaborador.getNombreCompleto()),
                escape(colaborador.getRolPrincipalNombre()),
                escape(colaborador.getUsername()),
                escape(colaborador.getEmail()),
                escape(baseUrl.replaceAll("/+$", ""))
        );
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
