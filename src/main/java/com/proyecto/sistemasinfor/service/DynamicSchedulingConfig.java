package com.proyecto.sistemasinfor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.proyecto.sistemasinfor.repository.UserRepository;

import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableScheduling
@Component
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    private final AppSettingsService settings;
    private final MailService mailService;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulingConfig.class);

    private ScheduledTaskRegistrar taskRegistrar;
    private ScheduledFuture<?> backupTask;
    private ScheduledFuture<?> fitnessTask;

    public DynamicSchedulingConfig(AppSettingsService settings, MailService mailService,
            UserRepository userRepository) {
        this.settings = settings;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar registrar) {
        this.taskRegistrar = registrar;
        log.info("=== Configurando tareas programadas ===");

        // Backup notification task
        log.info("Registrando tarea de Backup - Enabled: {}, Cron: {}",
                settings.isBackupEnabled(), settings.getBackupCron());
        backupTask = registrar.getScheduler().schedule(
                () -> {
                    if (!settings.isBackupEnabled())
                        return;
                    String message = settings.getBackupMessage();
                    String imagePath = settings.getBackupImage();
                    var users = userRepository.findAll();
                    log.info("[Scheduler] Enviando aviso de backup a {} usuarios (sin filtro de marketing)",
                            users.size());
                    users.forEach(user -> {
                        try {
                            if (imagePath != null && !imagePath.isEmpty()) {
                                mailService.sendEmailWithImage(
                                        user.getEmail(),
                                        "📅 Aviso de Copia de Seguridad",
                                        message,
                                        imagePath);
                            } else {
                                mailService.sendEmail(
                                        user.getEmail(),
                                        "📅 Aviso de Copia de Seguridad",
                                        message);
                            }
                        } catch (Exception ignored) {
                        }
                    });
                },
                nextExecutionFor(() -> settings.getBackupCron()));

        // Fitness recommendations task
        log.info("Registrando tarea de Fitness - Enabled: {}, Cron: {}",
                settings.isFitnessEnabled(), settings.getFitnessCron());
        fitnessTask = registrar.getScheduler().schedule(
                () -> {
                    if (!settings.isFitnessEnabled())
                        return;
                    String message = settings.getFitnessMessage();
                    String imagePath = settings.getFitnessImage();
                    var users = userRepository.findAll();
                    long targets = users.stream().filter(u -> Boolean.TRUE.equals(u.getMarketingEmailsAccepted()))
                            .count();
                    log.info("[Scheduler] Enviando recomendaciones fitness a {} usuarios (con marketing)", targets);
                    users.forEach(user -> {
                        if (Boolean.TRUE.equals(user.getMarketingEmailsAccepted())) {
                            try {
                                if (imagePath != null && !imagePath.isEmpty()) {
                                    mailService.sendEmailWithImage(
                                            user.getEmail(),
                                            "💪 Recomendaciones fitness",
                                            message,
                                            imagePath);
                                } else {
                                    mailService.sendEmail(
                                            user.getEmail(),
                                            "💪 Recomendaciones fitness",
                                            message);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });
                },
                nextExecutionFor(() -> settings.getFitnessCron()));
    }

    public void rescheduleBackupTask() {
        if (backupTask != null) {
            backupTask.cancel(false);
            log.info("Cancelando tarea de Backup anterior");
        }
        if (taskRegistrar != null && taskRegistrar.getScheduler() != null) {
            log.info("Re-programando tarea de Backup - Cron: {}", settings.getBackupCron());
            backupTask = taskRegistrar.getScheduler().schedule(
                    () -> {
                        if (!settings.isBackupEnabled())
                            return;
                        String message = settings.getBackupMessage();
                        String imagePath = settings.getBackupImage();
                        var users = userRepository.findAll();
                        log.info("[Scheduler] Enviando aviso de backup a {} usuarios", users.size());
                        users.forEach(user -> {
                            try {
                                if (imagePath != null && !imagePath.isEmpty()) {
                                    mailService.sendEmailWithImage(user.getEmail(), "📅 Aviso de Copia de Seguridad",
                                            message, imagePath);
                                } else {
                                    mailService.sendEmail(user.getEmail(), "📅 Aviso de Copia de Seguridad", message);
                                }
                            } catch (Exception ignored) {
                            }
                        });
                    },
                    nextExecutionFor(() -> settings.getBackupCron()));
        }
    }

    public void rescheduleFitnessTask() {
        if (fitnessTask != null) {
            fitnessTask.cancel(false);
            log.info("Cancelando tarea de Fitness anterior");
        }
        if (taskRegistrar != null && taskRegistrar.getScheduler() != null) {
            log.info("Re-programando tarea de Fitness - Cron: {}", settings.getFitnessCron());
            fitnessTask = taskRegistrar.getScheduler().schedule(
                    () -> {
                        if (!settings.isFitnessEnabled())
                            return;
                        String message = settings.getFitnessMessage();
                        String imagePath = settings.getFitnessImage();
                        var users = userRepository.findAll();
                        long targets = users.stream().filter(u -> Boolean.TRUE.equals(u.getMarketingEmailsAccepted()))
                                .count();
                        log.info("[Scheduler] Enviando recomendaciones fitness a {} usuarios", targets);
                        users.forEach(user -> {
                            if (Boolean.TRUE.equals(user.getMarketingEmailsAccepted())) {
                                try {
                                    if (imagePath != null && !imagePath.isEmpty()) {
                                        mailService.sendEmailWithImage(user.getEmail(), "💪 Recomendaciones fitness",
                                                message, imagePath);
                                    } else {
                                        mailService.sendEmail(user.getEmail(), "💪 Recomendaciones fitness", message);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });
                    },
                    nextExecutionFor(() -> settings.getFitnessCron()));
        }
    }

    private Trigger nextExecutionFor(java.util.function.Supplier<String> cronSupplier) {
        return triggerContext -> {
            String cron = cronSupplier.get();
            log.info("[Scheduler] Usando expresión cron: {}", cron);
            try {
                CronTrigger trigger = new CronTrigger(cron);
                var nextExecution = trigger.nextExecution(triggerContext);
                if (nextExecution != null) {
                    log.info("[Scheduler] Próxima ejecución programada para: {}", nextExecution);
                }
                return nextExecution;
            } catch (Exception ex) {
                log.error("[Scheduler] Error al parsear expresión cron '{}': {}", cron, ex.getMessage());
                // Si el cron es inválido, caer a una ejecución futura para no bloquear el
                // scheduler
                return new CronTrigger("0 0 3 * * *").nextExecution(triggerContext); // 03:00 diario como fallback
            }
        };
    }
}
