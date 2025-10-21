package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.model.*;
import com.proyecto.sistemasinfor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private LugarRepository lugarRepository;

    // Mostrar página de reservas del estudiante
    @GetMapping
    public String misReservas(HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        List<Reserva> reservas = reservaRepository.findByUsuarioOrderByFechaDescFechaCreacionDesc(usuario);
        List<Lugar> lugares = lugarRepository.findAll();
        
        model.addAttribute("reservas", reservas);
        model.addAttribute("lugares", lugares);
        model.addAttribute("nuevaReserva", new Reserva());
        
        return "reservas";
    }

    // Crear nueva reserva
    @PostMapping("/crear")
    public String crearReserva(@RequestParam Long lugarId,
                              @RequestParam String fecha,
                              @RequestParam String horaInicio,
                              @RequestParam String horaFin,
                              HttpSession session,
                              Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        try {
            Lugar lugar = lugarRepository.findById(lugarId)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));

            LocalDate fechaReserva = LocalDate.parse(fecha);
            LocalTime horaIni = LocalTime.parse(horaInicio);
            LocalTime horaFinal = LocalTime.parse(horaFin);

            // Validaciones
            if (fechaReserva.isBefore(LocalDate.now())) {
                model.addAttribute("error", "No puedes reservar en fechas pasadas.");
                return misReservas(session, model);
            }

            if (horaIni.isAfter(horaFinal) || horaIni.equals(horaFinal)) {
                model.addAttribute("error", "La hora de fin debe ser posterior a la hora de inicio.");
                return misReservas(session, model);
            }

            // Verificar que el lugar esté abierto
            if (lugar.getEstado() != null && !lugar.getEstado().equalsIgnoreCase("abierto")) {
                model.addAttribute("error", "El lugar no está disponible actualmente.");
                return misReservas(session, model);
            }

            // Verificar conflictos de horario
            List<Reserva> conflictos = reservaRepository.findConflictingReservations(
                lugar, fechaReserva, horaIni, horaFinal,
                Reserva.EstadoReserva.PENDIENTE,
                Reserva.EstadoReserva.APROBADA
            );
            if (!conflictos.isEmpty()) {
                model.addAttribute("error", "Ya existe una reserva en ese horario para este lugar.");
                return misReservas(session, model);
            }

            // Crear reserva
            Reserva reserva = new Reserva(usuario, lugar, fechaReserva, horaIni, horaFinal);
            reservaRepository.save(reserva);

            model.addAttribute("success", "Reserva creada correctamente. Está pendiente de aprobación.");
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear la reserva: " + e.getMessage());
        }

        return misReservas(session, model);
    }

    // Cancelar reserva
    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        try {
            Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            // Verificar que la reserva pertenece al usuario
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                model.addAttribute("error", "No tienes permisos para cancelar esta reserva.");
                return misReservas(session, model);
            }

            // Solo se pueden cancelar reservas pendientes o aprobadas
            if (reserva.getEstado() == Reserva.EstadoReserva.PENDIENTE || 
                reserva.getEstado() == Reserva.EstadoReserva.APROBADA) {
                reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
                reservaRepository.save(reserva);
                model.addAttribute("success", "Reserva cancelada correctamente.");
            } else {
                model.addAttribute("error", "Esta reserva no se puede cancelar.");
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error al cancelar la reserva: " + e.getMessage());
        }

        return misReservas(session, model);
    }
}