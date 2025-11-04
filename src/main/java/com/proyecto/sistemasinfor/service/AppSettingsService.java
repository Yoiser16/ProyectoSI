package com.proyecto.sistemasinfor.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.sistemasinfor.model.Setting;
import com.proyecto.sistemasinfor.repository.SettingRepository;

@Service
public class AppSettingsService {

    public static final String BACKUP_ENABLED = "backup.notification.enabled";
    public static final String BACKUP_CRON = "backup.notification.cron";
    public static final String BACKUP_MESSAGE = "backup.notification.message";
    public static final String BACKUP_IMAGE = "backup.notification.image";

    public static final String FITNESS_ENABLED = "fitness.recommendation.enabled";
    public static final String FITNESS_CRON = "fitness.recommendation.cron";
    public static final String FITNESS_MESSAGE = "fitness.recommendation.message";
    public static final String FITNESS_IMAGE = "fitness.recommendation.image";

    private static final String DEF_BACKUP_CRON = "0 0 9 * * MON"; // Lunes 09:00
    private static final String DEF_FITNESS_CRON = "0 0 10 * * MON"; // Lunes 10:00

    private final SettingRepository repo;

    public AppSettingsService(SettingRepository repo) {
        this.repo = repo;
    }

    public String getString(String key, String defVal) {
        Optional<Setting> s = repo.findByK(key);
        return s.map(Setting::getV).orElse(defVal);
    }

    public boolean getBoolean(String key, boolean defVal) {
        String v = getString(key, Boolean.toString(defVal));
        return Boolean.parseBoolean(v);
    }

    @Transactional
    public void set(String key, String value) {
        Setting s = repo.findByK(key).orElse(new Setting(key, value));
        s.setV(value);
        repo.save(s);
    }

    // Helper getters con valores por defecto
    public boolean isBackupEnabled() {
        return getBoolean(BACKUP_ENABLED, true);
    }

    public String getBackupCron() {
        return getString(BACKUP_CRON, DEF_BACKUP_CRON);
    }

    public String getBackupMessage() {
        return getString(BACKUP_MESSAGE, "Se realizará una copia de seguridad próximamente.");
    }

    public String getBackupImage() {
        return getString(BACKUP_IMAGE, "");
    }

    public boolean isFitnessEnabled() {
        return getBoolean(FITNESS_ENABLED, false);
    }

    public String getFitnessCron() {
        return getString(FITNESS_CRON, DEF_FITNESS_CRON);
    }

    public String getFitnessMessage() {
        return getString(FITNESS_MESSAGE, "¡Hora de moverse! Estas son tus recomendaciones fitness de la semana.");
    }

    public String getFitnessImage() {
        return getString(FITNESS_IMAGE, "");
    }
}
