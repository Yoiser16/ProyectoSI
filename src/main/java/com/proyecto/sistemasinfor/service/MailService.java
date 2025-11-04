package com.proyecto.sistemasinfor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.proyecto.sistemasinfor.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Restablecimiento de contraseña");
        message.setText("Haz clic en el siguiente enlace para restablecer tu contraseña: " + resetLink);
        mailSender.send(message);
    }

    public void sendEmail(String email, String subject, String body) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email de destino requerido");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendEmailWithImage(String email, String subject, String body, String imagePath) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email de destino requerido");
        }

        log.info("Enviando correo con imagen a: {}", email);
        log.info("Ruta de imagen: {}", imagePath);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // Helper solo para cabeceras (to, subject)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

            helper.setTo(Objects.requireNonNull(email));
            String nonNullSubject = (subject == null) ? "" : subject;
            helper.setSubject(nonNullSubject);

            // Construir HTML con imagen si existe
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>");
            htmlContent.append("<html>");
            htmlContent.append("<head>");
            htmlContent.append("<meta charset='UTF-8'>");
            htmlContent.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            htmlContent.append("</head>");
            htmlContent.append(
                    "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;'>");
            htmlContent.append(
                    "<div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>");

            // Añadir imagen si existe (solo referencia CID en el HTML; el adjunto real se
            // agrega más abajo)
            if (imagePath != null && !imagePath.isEmpty()) {
                // Buscar primero en src (desarrollo)
                File imageFile = Paths.get("src/main/resources/static" + imagePath).toFile();
                if (!imageFile.exists()) {
                    // fallback cuando corre compilado
                    imageFile = Paths.get("target/classes/static" + imagePath).toFile();
                }
                log.info("Buscando imagen en: {}", imageFile.getAbsolutePath());
                log.info("¿Existe la imagen?: {}", imageFile.exists());

                if (imageFile.exists()) {
                    htmlContent.append("<div style='text-align: center; margin-bottom: 20px;'>");
                    htmlContent.append(
                            "<img src='cid:emailImage' alt='Imagen' style='max-width: 100%; height: auto; border-radius: 8px; display: block; margin: 0 auto;' />");
                    htmlContent.append("</div>");
                } else {
                    log.warn("Imagen no encontrada en ninguna ruta conocida. Última ruta probada: {}",
                            imageFile.getAbsolutePath());
                }
            }

            // Añadir mensaje (convertir saltos de línea a HTML y escapar caracteres
            // especiales)
            htmlContent.append("<div style='color: #333333; font-size: 16px; line-height: 1.6;'>");

            // Escapar HTML y convertir saltos de línea
            String safeBody = (body == null) ? "" : body;
            String escapedBody = safeBody
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("\n", "<br>");

            htmlContent.append(escapedBody);
            htmlContent.append("</div>");

            // Footer
            htmlContent.append(
                    "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eeeeee; color: #999999; font-size: 14px; text-align: center;'>");
            htmlContent.append("<p>Sistema de Información - Universidad Remington</p>");
            htmlContent.append("</div>");

            htmlContent.append("</div>");
            htmlContent.append("</body>");
            htmlContent.append("</html>");

            log.info("Longitud del contenido HTML: {} caracteres", htmlContent.length());

            // Construir MIME manualmente para máxima compatibilidad
            // related -> (alternative -> [plain, html]), [inline-image]
            MimeMultipart related = new MimeMultipart("related");

            // Part alternative (plain + html)
            MimeMultipart alternative = new MimeMultipart("alternative");
            MimeBodyPart altWrapper = new MimeBodyPart();
            altWrapper.setContent(alternative);

            // Plain text
            MimeBodyPart plainPart = new MimeBodyPart();
            // reutilizar variable body (definida arriba) con null-safe
            String safeBodyAlt = (body == null) ? "" : body;
            plainPart.setText(safeBodyAlt, StandardCharsets.UTF_8.name());
            alternative.addBodyPart(plainPart);

            // HTML part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent.toString(), "text/html; charset=UTF-8");
            alternative.addBodyPart(htmlPart);

            // Añadir alternative a related
            related.addBodyPart(altWrapper);

            // Imagen inline si existe
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = Paths.get("src/main/resources/static" + imagePath).toFile();
                if (!imageFile.exists()) {
                    imageFile = Paths.get("target/classes/static" + imagePath).toFile();
                }
                if (imageFile.exists()) {
                    MimeBodyPart imagePart = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(imageFile);
                    imagePart.setDataHandler(new DataHandler(fds));
                    imagePart.setFileName(imageFile.getName());
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    imagePart.setHeader("Content-ID", "<emailImage>");
                    related.addBodyPart(imagePart);
                }
            }

            mimeMessage.setContent(related);
            mimeMessage.saveChanges();
            mailSender.send(mimeMessage);
            log.info("Correo enviado exitosamente a: {}", email);

        } catch (MessagingException e) {
            log.error("Error al enviar correo con imagen: {}", e.getMessage());
            // Si falla el HTML, enviar texto plano
            sendEmail(email, subject, body);
        }
    }

    public void sendFitnessRecommendations(User user) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Usuario o correo inválido");
        }
        String nombre = StringUtils.hasText(user.getNombre()) ? user.getNombre() : "usuario";
        String subject = "Tus recomendaciones fitness personalizadas";
        String body = "Hola " + nombre + ",\n\n"
                + "Aquí tienes algunas recomendaciones fitness para mejorar tu bienestar:\n"
                + "- Establece metas SMART (específicas, medibles, alcanzables, relevantes y con tiempo).\n"
                + "- Realiza 150 minutos/semana de actividad cardiovascular moderada o 75 de alta intensidad.\n"
                + "- Incluye 2-3 sesiones de fuerza (full body) por semana.\n"
                + "- Prioriza el descanso (7-8 horas) y la hidratación (30-35 ml/kg/día).\n"
                + "- Calienta 5-10 min antes y estira suavemente post-entrenamiento.\n\n"
                + "Consejo extra: registra tus entrenamientos y progreso para mantener la motivación.\n\n"
                + "¡Muchos ánimos!";
        sendEmail(user.getEmail(), subject, body);
    }

    public void sendConsentConfirmation(User user) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Usuario o correo inválido");
        }
        String nombre = StringUtils.hasText(user.getNombre()) ? user.getNombre() : "usuario";
        String subject = "Confirmación de consentimiento de datos";
        String marketingTexto = Boolean.TRUE.equals(user.getMarketingEmailsAccepted())
                ? "Has aceptado recibir comunicaciones informativas (fitness y notificaciones)."
                : "No recibirás comunicaciones de marketing. Puedes cambiar esta preferencia en tu perfil cuando quieras.";
        String body = "Hola " + nombre + ",\n\n"
                + "Gracias por aceptar nuestra Política de Privacidad y el tratamiento de datos personales.\n"
                + marketingTexto + "\n\n"
                + "Fecha de consentimiento: " + (user.getConsentDate() != null ? user.getConsentDate() : "N/D") + "\n\n"
                + "Puedes consultar la política completa en el sistema y modificar tus preferencias desde tu perfil.\n\n"
                + "Saludos,\nEquipo de Sistema Sinfor";
        sendEmail(user.getEmail(), subject, body);
    }
}