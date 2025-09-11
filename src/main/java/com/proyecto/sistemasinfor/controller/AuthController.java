package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.dto.LoginRequest;
import com.proyecto.sistemasinfor.dto.PasswordResetRequest;
import com.proyecto.sistemasinfor.dto.RegisterRequest;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.service.AuthService;
import com.proyecto.sistemasinfor.service.MailService;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private MailService mailService;

    // Mapa temporal para tokens (en producción usa base de datos)
    private Map<String, String> resetTokens = new HashMap<>();

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
            Model model, HttpSession session) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        Optional<User> userOpt = authService.login(request);
        if (userOpt.isPresent()) {
            session.setAttribute("usuario", userOpt.get().getEmail());
            session.setAttribute("nombreUsuario", userOpt.get().getNombre()); // <-- Agrega esta línea
            return "redirect:/menu";
        }
        model.addAttribute("error", "Credenciales inválidas");
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String nombre, @RequestParam String email, @RequestParam String password,
            Model model) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Todos los campos son obligatorios.");
            return "register";
        }

        String emailRegex = "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|outlook\\.com|yahoo\\.com)$";
        if (!email.matches(emailRegex)) {
            model.addAttribute("error", "Solo se permiten correos de Gmail, Hotmail, Outlook o Yahoo.");
            return "register";
        }

        // Validar si el correo ya está registrado
        Optional<User> existingUser = authService.findByEmail(email);
        if (existingUser.isPresent()) {
            model.addAttribute("error",
                    "El correo ya está registrado. Por favor, inicia sesión o recupera tu contraseña.");
            return "register";
        }

        // Validar contraseña segura
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).{8,}$";
        if (!password.matches(passwordRegex)) {
            model.addAttribute("error",
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial.");
            return "register";
        }

        RegisterRequest request = new RegisterRequest();
        request.setNombre(nombre);
        request.setEmail(email);
        request.setPassword(password);

        authService.register(request);
        return "redirect:/auth/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String newPassword, HttpSession session, Model model) {
        String email = (String) session.getAttribute("usuario");
        if (email == null) {
            model.addAttribute("error", "Debes iniciar sesión para cambiar tu contraseña.");
            return "reset-password";
        }

        // Validación de contraseña segura
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            model.addAttribute("error",
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial.");
            return "reset-password";
        }

        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail(email); // Solo el usuario en sesión
        request.setNewPassword(newPassword);

        boolean result = authService.resetPassword(request);
        if (result) {
            model.addAttribute("success", "Contraseña restablecida correctamente.");
        } else {
            model.addAttribute("error", "No se pudo cambiar la contraseña.");
        }
        return "reset-password";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password"; // Debe coincidir con el nombre del archivo HTML en templates
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        Optional<User> userOpt = authService.findByEmail(email);
        if (userOpt.isPresent()) {
            String token = UUID.randomUUID().toString();
            resetTokens.put(token, email);
            String resetLink = "http://localhost:8080/auth/reset-password-form?token=" + token;
            mailService.sendPasswordResetEmail(email, resetLink);
            model.addAttribute("success", "Se ha enviado un enlace de recuperación a tu correo.");
        } else {
            model.addAttribute("error", "Correo no registrado.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password-form")
    public String showResetPasswordFormWithToken(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password-form";
    }

    @PostMapping("/reset-password-form")
    public String processResetPassword(@RequestParam String token,
            @RequestParam String newPassword,
            Model model) {

        // Validación de contraseña segura
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;\"'<>,.?/]).{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            model.addAttribute("error",
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial.");
            model.addAttribute("token", token);
            return "reset-password-form";
        }

        String email = resetTokens.get(token);
        if (email != null) {
            PasswordResetRequest request = new PasswordResetRequest();
            request.setEmail(email);
            request.setNewPassword(newPassword);
            boolean result = authService.resetPassword(request);
            if (result) {
                resetTokens.remove(token);
                model.addAttribute("success", "Contraseña restablecida correctamente. Ahora puedes iniciar sesión.");
                return "login";
            }
        }
        model.addAttribute("error", "El enlace es inválido o ha expirado.");
        model.addAttribute("token", token);
        return "reset-password-form";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
