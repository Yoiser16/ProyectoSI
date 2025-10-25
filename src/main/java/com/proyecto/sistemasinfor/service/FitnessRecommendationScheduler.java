package com.proyecto.sistemasinfor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.proyecto.sistemasinfor.repository.UserRepository;

@Service
public class FitnessRecommendationScheduler {

    private final MailService mailService;
    private final UserRepository userRepository;

    @Value("${fitness.recommendation.enabled:false}")
    private boolean enabled;

    public FitnessRecommendationScheduler(MailService mailService, UserRepository userRepository) {
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    // Por defecto: cada lunes a las 10:00
    @Scheduled(cron = "${fitness.recommendation.cron:0 0 10 * * MON}")
    public void sendRecommendationsWeekly() {
        if (!enabled) return;
        userRepository.findAll().forEach(user -> {
            // Solo enviar a usuarios que aceptaron recibir correos de marketing
            if (Boolean.TRUE.equals(user.getMarketingEmailsAccepted())) {
                try {
                    mailService.sendFitnessRecommendations(user);
                } catch (Exception ignored) {
                    // Log en un sistema real
                }
            }
        });
    }
}
