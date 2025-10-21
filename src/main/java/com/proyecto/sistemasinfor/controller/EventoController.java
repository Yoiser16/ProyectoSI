package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.model.Evento;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    // Mostrar página de eventos con calendario
    @GetMapping
    public String verEventos(HttpSession session, Model model) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        // Obtener todos los eventos para mostrar en lista y calendario
        List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
        model.addAttribute("eventos", eventos);
        
        return "eventos";
    }

    // API para obtener eventos para el calendario (JSON)
    @GetMapping("/api/calendario")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getEventosCalendario() {
        List<Evento> eventos = eventoRepository.findAllByOrderByFechaAsc();
        
        List<Map<String, Object>> eventosJson = eventos.stream().map(evento -> {
            Map<String, Object> eventoMap = new HashMap<>();
            eventoMap.put("id", evento.getId());
            eventoMap.put("title", evento.getTitulo());
            // Combinar fecha + hora para el calendario
            eventoMap.put("fecha", evento.getFecha().toString());
            eventoMap.put("horaInicio", evento.getHoraInicio().toString());
            eventoMap.put("horaFin", evento.getHoraFin().toString());
            eventoMap.put("description", evento.getDescripcion());
            eventoMap.put("location", evento.getLugar() != null ? evento.getLugar().getNombre() : "Sin ubicación");
            return eventoMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosJson);
    }

    // Ver detalle de evento
    @GetMapping("/detalle/{id}")
    public String verDetalleEvento(@PathVariable Long id, Model model, HttpSession session) {
        User usuario = (User) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        try {
            Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            
            model.addAttribute("evento", evento);
            return "detalle-evento";
            
        } catch (Exception e) {
            return "redirect:/eventos";
        }
    }
}