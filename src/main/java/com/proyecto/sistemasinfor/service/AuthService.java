package com.proyecto.sistemasinfor.service;

import com.proyecto.sistemasinfor.dto.LoginRequest;
import com.proyecto.sistemasinfor.dto.RegisterRequest;
import com.proyecto.sistemasinfor.dto.PasswordResetRequest;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @org.springframework.beans.factory.annotation.Value("${auth.max-attempts:3}")
    private int maxFailedAttempts;

    @org.springframework.beans.factory.annotation.Value("${auth.lock-duration-minutes:0}")
    private int lockDurationMinutes; // 0 = bloqueo hasta enlace de desbloqueo

    public Optional<User> login(LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // Verificar si la cuenta está bloqueada y si corresponde auto-desbloqueo por tiempo
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            if (lockDurationMinutes > 0 && user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
                if (unlockTime.isBefore(LocalDateTime.now())) {
                    // Auto-desbloquear por expiración del periodo de bloqueo
                    user.setAccountLocked(false);
                    user.setFailedAttempts(0);
                    user.setLockTime(null);
                    user.setUnlockToken(null);
                    userService.saveUser(user);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }

        // Verificar contraseña
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Login exitoso - resetear intentos fallidos
            if (user.getFailedAttempts() != null && user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                userService.saveUser(user);
            }
            return Optional.of(user);
        } else {
            // Login fallido - incrementar contador
            increaseFailedAttempts(user);
            return Optional.empty();
        }
    }

    public int getRemainingAttempts(String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int failedAttempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
            return Math.max(0, maxFailedAttempts - failedAttempts);
        }
        return maxFailedAttempts;
    }

    public User register(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getNombre(), request.getEmail(), encodedPassword);
        // Rol por defecto para nuevos registros
        user.setRol(Role.STUDENT);
        return userService.saveUser(user);
    }

    public User registerWithRole(RegisterRequest request, Role role) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getNombre(), request.getEmail(), encodedPassword);
        user.setRol(role != null ? role : Role.STUDENT);
        return userService.saveUser(user);
    }

    public boolean resetPassword(PasswordResetRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.saveUser(user);
            return true;
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }

    private void increaseFailedAttempts(User user) {
        int newFailAttempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1;
        user.setFailedAttempts(newFailAttempts);

        if (newFailAttempts >= maxFailedAttempts) {
            lockAccount(user);
        } else {
            userService.saveUser(user);
        }
    }

    private void lockAccount(User user) {
        user.setAccountLocked(true);
        user.setLockTime(LocalDateTime.now());
        
        // Generar token único para desbloqueo
        String unlockToken = UUID.randomUUID().toString();
        user.setUnlockToken(unlockToken);
        
        userService.saveUser(user);

        // Enviar correo de notificación con enlace de desbloqueo
        try {
            String unlockUrl = "http://localhost:8080/auth/unlock-account?token=" + unlockToken;
            String subject = "🔒 Cuenta Bloqueada - Acción Requerida";
            String message = String.format(
                "Hola %s,\n\n" +
                "Tu cuenta ha sido bloqueada debido a 3 intentos fallidos de inicio de sesión.\n\n" +
                "Por seguridad, tu cuenta ha sido temporalmente bloqueada.\n\n" +
                "Para desbloquear tu cuenta, haz clic en el siguiente enlace:\n" +
                "%s\n\n" +
                "Si no intentaste iniciar sesión, te recomendamos cambiar tu contraseña de inmediato.\n\n" +
                "Este enlace expirará en 24 horas.\n\n" +
                "Saludos,\n" +
                "Equipo de Soporte",
                user.getNombre(),
                unlockUrl
            );
            mailService.sendEmail(user.getEmail(), subject, message);
        } catch (Exception e) {
            // Log error pero no fallar el proceso de bloqueo
            System.err.println("Error al enviar correo de bloqueo: " + e.getMessage());
        }
    }

    public boolean unlockAccount(String token) {
        Optional<User> userOpt = userService.findByUnlockToken(token);
        
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        
        // Verificar que el token no haya expirado (24 horas)
        if (user.getLockTime() != null && 
            user.getLockTime().plusHours(24).isBefore(LocalDateTime.now())) {
            return false;
        }

        // Desbloquear cuenta
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        user.setUnlockToken(null);
        userService.saveUser(user);

        return true;
    }
}