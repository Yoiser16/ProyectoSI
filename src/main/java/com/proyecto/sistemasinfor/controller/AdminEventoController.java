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
@RequestMapping("/admin/eventos")
public class AdminEventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private LugarRepository lugarRepository;

    private boolean tienePermiso(HttpSession session) {
        Object rol = session.getAttribute("rol");
        return rol == Role.ADMIN_ESPACIOS || rol == Role.ADMIN_TI;
    }

    // Listar eventos
    @GetMapping
    public String listarEventos(HttpSession session, Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
            List<Lugar> lugares = lugarRepository.findAll();
            
            model.addAttribute("eventos", eventos != null ? eventos : new java.util.ArrayList<>());
            model.addAttribute("lugares", lugares != null ? lugares : new java.util.ArrayList<>());
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar eventos: " + e.getMessage());
            model.addAttribute("eventos", new java.util.ArrayList<>());
            model.addAttribute("lugares", new java.util.ArrayList<>());
        }
        
        return "admin-eventos";
    }

    // Mostrar formulario para nuevo evento
    @GetMapping("/nuevo")
    public String nuevoEvento(HttpSession session, Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        List<Lugar> lugares = lugarRepository.findAll();
        model.addAttribute("evento", new Evento());
        model.addAttribute("lugares", lugares);
        
        return "editar-evento";
    }

    // Mostrar formulario para editar evento
    @GetMapping("/editar/{id}")
    public String editarEvento(@PathVariable Long id, HttpSession session, Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            
            List<Lugar> lugares = lugarRepository.findAll();
            model.addAttribute("evento", evento);
            model.addAttribute("lugares", lugares);
            
            return "editar-evento";
            
        } catch (Exception e) {
            return "redirect:/admin/eventos";
        }
    }

    // Crear nuevo evento
    @PostMapping("/crear")
    public String crearEvento(@RequestParam String titulo,
                             @RequestParam String fecha,
                             @RequestParam String horaInicio,
                             @RequestParam String horaFin,
                             @RequestParam Long lugarId,
                             @RequestParam(required = false) String descripcion,
                             HttpSession session,
                             Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            // Validar campos obligatorios antes de cualquier operación en BD
            if (titulo == null || titulo.trim().isEmpty() || fecha == null || fecha.trim().isEmpty() ||
                horaInicio == null || horaInicio.trim().isEmpty() || horaFin == null || horaFin.trim().isEmpty() || lugarId == null) {
                model.addAttribute("error", "Todos los campos obligatorios deben estar completos.");
            } else {
                // Buscar el lugar sólo después de validar
                Lugar lugar = lugarRepository.findById(lugarId)
                    .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));

                LocalDate fechaEvento = LocalDate.parse(fecha);
                LocalTime horaInicioEvento = LocalTime.parse(horaInicio);
                LocalTime horaFinEvento = LocalTime.parse(horaFin);
                if (!horaFinEvento.isAfter(horaInicioEvento)) {
                    model.addAttribute("error", "La hora de fin debe ser posterior a la hora de inicio");
                } else {
                    Evento evento = new Evento(titulo, descripcion, lugar, fechaEvento, horaInicioEvento, horaFinEvento);
                    eventoRepository.save(evento);
                    model.addAttribute("success", "Evento creado exitosamente");
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el evento: " + e.getMessage());
        }
        // Limpiar el formulario y mostrar la lista actualizada
        model.addAttribute("evento", new Evento());
        List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
        List<Lugar> lugares = lugarRepository.findAll();
        model.addAttribute("eventos", eventos);
        model.addAttribute("lugares", lugares);
        return "admin-eventos";
    }

    // Actualizar evento existente
    @PostMapping("/actualizar")
    public String actualizarEvento(@RequestParam Long id,
                                  @RequestParam String titulo,
                                  @RequestParam String fecha,
                                  @RequestParam String horaInicio,
                                  @RequestParam String horaFin,
                                  @RequestParam Long lugarId,
                                  @RequestParam(required = false) String descripcion,
                                  HttpSession session,
                                  Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            Lugar lugar = lugarRepository.findById(lugarId)
                .orElseThrow(() -> new RuntimeException("Lugar no encontrado"));
            
            // Convertir fecha y horas
            LocalDate fechaEvento = LocalDate.parse(fecha);
            LocalTime horaInicioEvento = LocalTime.parse(horaInicio);
            LocalTime horaFinEvento = LocalTime.parse(horaFin);
            
            evento.setTitulo(titulo);
            evento.setFecha(fechaEvento);
            evento.setHoraInicio(horaInicioEvento);
            evento.setHoraFin(horaFinEvento);
            evento.setLugar(lugar);
            evento.setDescripcion(descripcion);
            
            // Validar que la hora de fin sea posterior a la de inicio
            if (!horaFinEvento.isAfter(horaInicioEvento)) {
                model.addAttribute("error", "La hora de fin debe ser posterior a la hora de inicio");
                List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
                List<Lugar> lugares = lugarRepository.findAll();
                model.addAttribute("eventos", eventos);
                model.addAttribute("lugares", lugares);
                return "admin-eventos";
            }
            
            eventoRepository.save(evento);
            model.addAttribute("success", "Evento actualizado exitosamente");
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el evento: " + e.getMessage());
        }

        List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
        List<Lugar> lugares = lugarRepository.findAll();
        model.addAttribute("eventos", eventos);
        model.addAttribute("lugares", lugares);
        return "admin-eventos";
    }

    // Eliminar evento
    @PostMapping("/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id, HttpSession session, Model model) {
        if (!tienePermiso(session)) return "redirect:/menu";

        try {
            eventoRepository.deleteById(id);
            model.addAttribute("success", "Evento eliminado exitosamente");
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar el evento: " + e.getMessage());
        }

        List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
        List<Lugar> lugares = lugarRepository.findAll();
        model.addAttribute("eventos", eventos);
        model.addAttribute("lugares", lugares);
        return "admin-eventos";
    }
}