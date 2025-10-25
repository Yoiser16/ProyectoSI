package com.proyecto.sistemasinfor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.proyecto.sistemasinfor.repository.UserRepository;

@Service
public class BackupNotificationScheduler {

    private final MailService mailService;
    private final UserRepository userRepository;

    @Value("${backup.notification.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${backup.notification.message:Se realizará una copia de seguridad próximamente.}")
    private String notifyMessage;

    public BackupNotificationScheduler(MailService mailService, UserRepository userRepository) {
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    // Cron configurable: por defecto, todos los lunes a las 09:00
    @Scheduled(cron = "${backup.notification.cron:0 0 9 * * MON}")
    public void sendWeeklyBackupReminder() {
        if (!notificationsEnabled) return;
        
        // Enviar notificación solo a usuarios que aceptaron recibir correos de marketing
        userRepository.findAll().forEach(user -> {
            if (Boolean.TRUE.equals(user.getMarketingEmailsAccepted())) {
                try {
                    mailService.sendEmail(
                        user.getEmail(), 
                        "📅 Aviso de Copia de Seguridad", 
                        notifyMessage
                    );
                } catch (Exception ignored) {
                    // En un sistema real, loggear el error
                }
            }
        });
    }
}
