package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.model.Evento;
import com.proyecto.sistemasinfor.model.Reserva;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.model.Role;
import com.proyecto.sistemasinfor.repository.EventoRepository;
import com.proyecto.sistemasinfor.repository.ReservaRepository;
import com.proyecto.sistemasinfor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
public class MenuController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {
        Object userObj = session.getAttribute("usuario");
        if (userObj == null) {
            return "redirect:/auth/login";
        }
        User usuario = (User) userObj;

        // Próximo evento (si existe)
        List<Evento> futuros = eventoRepository.findEventosFuturos();
        if (!futuros.isEmpty()) {
            Evento next = futuros.get(0);
            model.addAttribute("nextEvent", next);
        }

        // Resumen por rol
        Role rol = usuario.getRol();
        if (rol == Role.STUDENT) {
            List<Reserva> mias = reservaRepository.findByUsuarioOrderByFechaDescFechaCreacionDesc(usuario);
            long pendientes = mias.stream().filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE).count();
            model.addAttribute("misPendientes", pendientes);

            mias.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.APROBADA && !r.getFecha().isBefore(LocalDate.now()))
                .min(Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHoraInicio))
                .ifPresent(r -> model.addAttribute("miProximaReserva", r));

        } else if (rol == Role.ADMIN_ESPACIOS) {
            long porAprobar = reservaRepository.findByEstadoOrderByFechaCreacionDesc(Reserva.EstadoReserva.PENDIENTE).size();
            model.addAttribute("reservasPorAprobar", porAprobar);
        } else if (rol == Role.ADMIN_TI) {
            long totalUsuarios = userRepository.count();
            model.addAttribute("totalUsuarios", totalUsuarios);
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
