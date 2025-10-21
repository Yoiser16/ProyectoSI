package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.model.*;
import com.proyecto.sistemasinfor.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/admin/reservas")
public class AdminReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    private boolean tienePermiso(HttpSession session) {
        Object rol = session.getAttribute("rol");
        return rol == Role.ADMIN_ESPACIOS || rol == Role.ADMIN_TI;
    }

    // Listar reservas pendientes de aprobación
    @GetMapping
    public String listarReservasPendientes(HttpSession session, Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            List<Reserva> reservasPendientes = reservaRepository.findByEstadoOrderByFechaCreacionAsc(Reserva.EstadoReserva.PENDIENTE);
            List<Reserva> todasReservas = reservaRepository.findAll();
            
            model.addAttribute("reservasPendientes", reservasPendientes != null ? reservasPendientes : new java.util.ArrayList<>());
            model.addAttribute("todasReservas", todasReservas != null ? todasReservas : new java.util.ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar reservas: " + e.getMessage());
            model.addAttribute("reservasPendientes", new java.util.ArrayList<>());
            model.addAttribute("todasReservas", new java.util.ArrayList<>());
        }
        
        return "admin-reservas";
    }

    // Aprobar reserva
    @PostMapping("/aprobar/{id}")
    public String aprobarReserva(@PathVariable Long id, HttpSession session) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE) {
                reserva.setEstado(Reserva.EstadoReserva.APROBADA);
                reserva.setMotivoRechazo(null); // Limpiar motivo de rechazo si existía
                reservaRepository.save(reserva);
            }

        } catch (Exception e) {
            // Log error
        }

        return "redirect:/admin/reservas";
    }

    // Rechazar reserva
    @PostMapping("/rechazar/{id}")
    public String rechazarReserva(@PathVariable Long id, 
                                 @RequestParam(required = false) String motivo,
                                 HttpSession session) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE) {
                reserva.setEstado(Reserva.EstadoReserva.RECHAZADA);
                reserva.setMotivoRechazo(motivo != null ? motivo : "Sin motivo especificado");
                reservaRepository.save(reserva);
            }

        } catch (Exception e) {
            // Log error
        }

        return "redirect:/admin/reservas";
    }

    // Ver detalles de reserva
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, HttpSession session, Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            model.addAttribute("reserva", reserva);
            return "detalle-reserva";

        } catch (Exception e) {
            return "redirect:/admin/reservas";
        }
    }
}