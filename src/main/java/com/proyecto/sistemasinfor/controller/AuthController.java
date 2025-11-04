package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.dto.LoginRequest;
import com.proyecto.sistemasinfor.dto.PasswordResetRequest;
import com.proyecto.sistemasinfor.dto.RegisterRequest;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.model.Role;
import com.proyecto.sistemasinfor.service.AuthService;
import com.proyecto.sistemasinfor.service.MailService;
import com.proyecto.sistemasinfor.service.UserService;
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

    @Autowired
    private UserService userService;

    // Mapa temporal para tokens (en producción usa base de datos)
    private Map<String, String> resetTokens = new HashMap<>();

    @GetMapping("/seleccionar-perfil")
    public String seleccionarPerfilPage() {
        return "seleccionar-perfil";
    }

    @PostMapping("/seleccionar-perfil")
    public String seleccionarPerfil(@RequestParam("rol") String rol, HttpSession session) {
        // Validar y guardar rol seleccionado en sesión
        try {
            Role role = Role.valueOf(rol);
            session.setAttribute("rolSeleccionado", role);
        } catch (IllegalArgumentException ex) {
            session.removeAttribute("rolSeleccionado");
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        Object rolSel = session.getAttribute("rolSeleccionado");
        if (rolSel == null) {
            return "redirect:/auth/seleccionar-perfil";
        }
        model.addAttribute("rolSeleccionado", rolSel.toString());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
            Model model, HttpSession session) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        // Verificar si la cuenta está bloqueada antes de intentar login
        Optional<User> userCheck = authService.findByEmail(email);
        if (userCheck.isPresent() && Boolean.TRUE.equals(userCheck.get().getAccountLocked())) {
            model.addAttribute("error",
                    "🔒 Tu cuenta ha sido bloqueada por múltiples intentos fallidos. Revisa tu correo para desbloquearla.");
            Object rolSel = session.getAttribute("rolSeleccionado");
            model.addAttribute("rolSeleccionado", rolSel != null ? rolSel.toString() : "");
            return "login";
        }

        Optional<User> userOpt = authService.login(request);
        if (userOpt.isPresent()) {
            User usuario = userOpt.get();
            // Validar que el usuario tenga el rol seleccionado
            Role rolSeleccionado = (Role) session.getAttribute("rolSeleccionado");
            if (rolSeleccionado != null && usuario.getRol() != null && usuario.getRol() != rolSeleccionado) {
                model.addAttribute("error", "No tienes permisos para el perfil seleccionado. Elige otro perfil.");
                model.addAttribute("rolSeleccionado", rolSeleccionado.toString());
                return "login";
            }
            session.setAttribute("usuario", usuario);
            session.setAttribute("nombreUsuario", usuario.getNombre());
            session.setAttribute("rol", usuario.getRol());
            session.setAttribute("userRole", usuario.getRol().name()); // Agregar para compatibilidad con las vistas

            // Verificar si ha aceptado el consentimiento de privacidad
            if (usuario.getPrivacyPolicyAccepted() == null || !usuario.getPrivacyPolicyAccepted()) {
                return "redirect:/auth/consentimiento";
            }

            return "redirect:/menu";
        }

        // Login fallido - mostrar intentos restantes
        int remainingAttempts = authService.getRemainingAttempts(email);
        if (remainingAttempts > 0) {
            model.addAttribute("error", "⚠️ Credenciales inválidas. Te quedan " + remainingAttempts
                    + " intento(s) antes de que tu cuenta sea bloqueada.");
        } else {
            model.addAttribute("error", "🔒 Tu cuenta ha sido bloqueada. Revisa tu correo para desbloquearla.");
        }

        model.addAttribute("rolSeleccionado", String.valueOf(session.getAttribute("rolSeleccionado")));
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String nombre, @RequestParam String email, @RequestParam String password,
            Model model, HttpSession session) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Todos los campos son obligatorios.");
            return "register";
        }

        // Validar dominios permitidos por rol (mantener dominios base y agregar
        // institucionales)
        Role rolSel = (Role) session.getAttribute("rolSeleccionado");
        if (!authService.isAllowedEmailForRole(email, rolSel != null ? rolSel : Role.STUDENT)) {
            model.addAttribute("error",
                    "Correo no permitido para el perfil seleccionado. Permitidos: Gmail/Hotmail/Outlook/Yahoo, "
                            + "o institucional @miremington.edu.co para Estudiantes y @uniremington.edu.co para otros roles.");
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

        if (rolSel != null) {
            authService.registerWithRole(request, rolSel);
        } else {
            authService.register(request);
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String newPassword, HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
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
        request.setEmail(usuario.getEmail()); // Solo el usuario en sesión
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

    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("usuario") User usuarioForm, HttpSession session, Model model) {
        User usuarioSesion = (User) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/auth/login";
        }
        usuarioSesion.setNombre(usuarioForm.getNombre());
        userService.actualizarUsuario(usuarioSesion);
        session.setAttribute("usuario", usuarioSesion);
        model.addAttribute("usuario", usuarioSesion);
        model.addAttribute("mensaje", "Perfil actualizado correctamente.");
        return "perfil";
    }

    // Enviar recomendaciones fitness al correo del usuario actual
    @PostMapping("/enviar-recomendaciones")
    public String enviarRecomendaciones(HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        try {
            mailService.sendFitnessRecommendations(usuario);
            model.addAttribute("success", "Te enviamos recomendaciones fitness a tu correo.");
        } catch (Exception ex) {
            model.addAttribute("error", "No se pudo enviar el correo: " + ex.getMessage());
        }
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    // Desbloquear cuenta con token
    @GetMapping("/unlock-account")
    public String unlockAccount(@RequestParam("token") String token, Model model) {
        boolean success = authService.unlockAccount(token);

        if (success) {
            model.addAttribute("mensaje", "Tu cuenta ha sido desbloqueada exitosamente. Ya puedes iniciar sesión.");
            model.addAttribute("tipo", "success");
        } else {
            model.addAttribute("mensaje",
                    "El enlace de desbloqueo es inválido o ha expirado. Por favor contacta al soporte.");
            model.addAttribute("tipo", "error");
        }

        return "unlock-account-result";
    }

    // Mostrar página de consentimiento
    @GetMapping("/consentimiento")
    public String mostrarConsentimiento(HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        return "consentimiento";
    }

    // Guardar consentimiento del usuario
    @PostMapping("/guardar-consentimiento")
    public String guardarConsentimiento(
            @RequestParam(name = "privacyPolicyAccepted", required = false) String privacyAccepted,
            @RequestParam(name = "marketingEmailsAccepted", required = false) String marketingAccepted,
            HttpSession session,
            Model model) {

        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        // Validar que aceptó la política de privacidad (obligatorio)
        if (privacyAccepted == null || !privacyAccepted.equals("on")) {
            model.addAttribute("mensaje", "Debes aceptar la Política de Privacidad para continuar usando el sistema.");
            return "consentimiento";
        }

        // Actualizar preferencias del usuario
        usuario.setPrivacyPolicyAccepted(true);
        usuario.setMarketingEmailsAccepted(marketingAccepted != null && marketingAccepted.equals("true"));
        usuario.setConsentDate(java.time.LocalDateTime.now());

        userService.saveUser(usuario);

        // Enviar correo de confirmación de consentimiento
        try {
            mailService.sendConsentConfirmation(usuario);
        } catch (Exception e) {
            // No bloquear el flujo por error de correo
        }

        // Actualizar usuario en sesión
        session.setAttribute("usuario", usuario);

        return "redirect:/menu";
    }
}
