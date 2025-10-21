package com.proyecto.sistemasinfor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MenuController {

    @GetMapping("/")
    public String home() {
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String menu(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/auth/login";
        }
        return "menu";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/perfil")
    public String perfil() {
        return "redirect:/auth/perfil";
    }
}
