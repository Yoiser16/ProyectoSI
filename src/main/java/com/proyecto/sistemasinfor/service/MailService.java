package com.proyecto.sistemasinfor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.proyecto.sistemasinfor.model.User;

@Service
public class MailService {

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