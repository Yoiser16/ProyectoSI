package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.model.Role;
import com.proyecto.sistemasinfor.service.AppSettingsService;
import com.proyecto.sistemasinfor.service.DynamicSchedulingConfig;
import com.proyecto.sistemasinfor.service.MailService;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin/config")
public class AdminConfigController {

    private final AppSettingsService settings;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final DynamicSchedulingConfig schedulingConfig;

    public AdminConfigController(AppSettingsService settings, MailService mailService, UserRepository userRepository,
            DynamicSchedulingConfig schedulingConfig) {
        this.settings = settings;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.schedulingConfig = schedulingConfig;
    }

    private boolean isAdmin(HttpSession session) {
        Object roleObj = session.getAttribute("rol");
        if (roleObj instanceof Role) {
            Role r = (Role) roleObj;
            return r == Role.ADMIN_ESPACIOS || r == Role.ADMIN_TI;
        }
        return false;
    }

    @GetMapping
    public String configPage(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/auth/login";

        model.addAttribute("backupEnabled", settings.isBackupEnabled());
        model.addAttribute("backupCron", settings.getBackupCron());
        model.addAttribute("backupMessage", settings.getBackupMessage());
        model.addAttribute("backupImage", settings.getBackupImage());

        model.addAttribute("fitnessEnabled", settings.isFitnessEnabled());
        model.addAttribute("fitnessCron", settings.getFitnessCron());
        model.addAttribute("fitnessMessage", settings.getFitnessMessage());
        model.addAttribute("fitnessImage", settings.getFitnessImage());
        return "admin-config";
    }

    @PostMapping("/save")
    public String saveConfig(
            @RequestParam(name = "backupEnabled", required = false) String backupEnabled,
            @RequestParam(name = "backupCron") String backupCron,
            @RequestParam(name = "backupMessage") String backupMessage,
            @RequestParam(name = "backupImage", required = false) MultipartFile backupImage,
            @RequestParam(name = "fitnessEnabled", required = false) String fitnessEnabled,
            @RequestParam(name = "fitnessCron") String fitnessCron,
            @RequestParam(name = "fitnessMessage") String fitnessMessage,
            @RequestParam(name = "fitnessImage", required = false) MultipartFile fitnessImage,
            HttpSession session, Model model) {

        if (!isAdmin(session))
            return "redirect:/auth/login";

        // Guardar valores
        settings.set(AppSettingsService.BACKUP_ENABLED, String.valueOf("on".equals(backupEnabled)));
        settings.set(AppSettingsService.BACKUP_CRON, backupCron.trim());
        settings.set(AppSettingsService.BACKUP_MESSAGE, backupMessage);

        // Guardar imagen de backup si se subió
        if (backupImage != null && !backupImage.isEmpty()) {
            String imagePath = saveImage(backupImage, "backup");
            if (imagePath != null) {
                settings.set(AppSettingsService.BACKUP_IMAGE, imagePath);
            }
        }

        settings.set(AppSettingsService.FITNESS_ENABLED, String.valueOf("on".equals(fitnessEnabled)));
        settings.set(AppSettingsService.FITNESS_CRON, fitnessCron.trim());
        settings.set(AppSettingsService.FITNESS_MESSAGE, fitnessMessage);

        // Guardar imagen de fitness si se subió
        if (fitnessImage != null && !fitnessImage.isEmpty()) {
            String imagePath = saveImage(fitnessImage, "fitness");
            if (imagePath != null) {
                settings.set(AppSettingsService.FITNESS_IMAGE, imagePath);
            }
        }

        // Re-programar las tareas con los nuevos horarios
        schedulingConfig.rescheduleBackupTask();
        schedulingConfig.rescheduleFitnessTask();

        model.addAttribute("success", "Configuración guardada correctamente.");
        return "redirect:/admin/config";
    }

    private String saveImage(MultipartFile file, String prefix) {
        try {
            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return null;
            }

            // Obtener extensión del archivo
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Crear nombre único para el archivo
            String filename = prefix + "_" + UUID.randomUUID().toString() + extension;

            // Crear directorio si no existe
            Path uploadDir = Paths.get("src/main/resources/static/img/config");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Guardar archivo
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retornar la ruta relativa para usar en HTML
            return "/img/config/" + filename;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/test/backup")
    public String testBackup(@RequestParam(name = "to", required = false) String to,
            HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/auth/login";
        String message = settings.getBackupMessage();
        String imagePath = settings.getBackupImage();

        if (to != null && !to.isBlank()) {
            if (imagePath != null && !imagePath.isEmpty()) {
                mailService.sendEmailWithImage(to.trim(), "📅 Aviso de Copia de Seguridad (prueba)", message,
                        imagePath);
            } else {
                mailService.sendEmail(to.trim(), "📅 Aviso de Copia de Seguridad (prueba)", message);
            }
        } else {
            // Elegir un destinatario por defecto: primer admin o primer usuario
            User dest = userRepository.findAll().stream()
                    .filter(u -> u.getRol() == Role.ADMIN_ESPACIOS || u.getRol() == Role.ADMIN_TI)
                    .findFirst()
                    .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
            if (dest != null) {
                if (imagePath != null && !imagePath.isEmpty()) {
                    mailService.sendEmailWithImage(dest.getEmail(), "📅 Aviso de Copia de Seguridad (prueba)", message,
                            imagePath);
                } else {
                    mailService.sendEmail(dest.getEmail(), "📅 Aviso de Copia de Seguridad (prueba)", message);
                }
            }
        }
        return "redirect:/admin/config";
    }

    @PostMapping("/test/fitness")
    public String testFitness(@RequestParam(name = "to", required = false) String to,
            HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/auth/login";
        String message = settings.getFitnessMessage();
        String imagePath = settings.getFitnessImage();

        if (to != null && !to.isBlank()) {
            if (imagePath != null && !imagePath.isEmpty()) {
                mailService.sendEmailWithImage(to.trim(), "💪 Recomendaciones fitness (prueba)", message, imagePath);
            } else {
                mailService.sendEmail(to.trim(), "💪 Recomendaciones fitness (prueba)", message);
            }
        } else {
            // Elegir destinatario de prueba: alguien con marketing aceptado, si no, el
            // primero
            User dest = userRepository.findAll().stream()
                    .filter(u -> Boolean.TRUE.equals(u.getMarketingEmailsAccepted()))
                    .findFirst()
                    .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
            if (dest != null) {
                if (imagePath != null && !imagePath.isEmpty()) {
                    mailService.sendEmailWithImage(dest.getEmail(), "💪 Recomendaciones fitness (prueba)", message,
                            imagePath);
                } else {
                    mailService.sendEmail(dest.getEmail(), "💪 Recomendaciones fitness (prueba)", message);
                }
            }
        }
        return "redirect:/admin/config";
    }
}
