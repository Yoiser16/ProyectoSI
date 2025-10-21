package com.proyecto.sistemasinfor.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.proyecto.sistemasinfor.repository.EventoRepository;
import com.proyecto.sistemasinfor.repository.LugarRepository;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final EventoRepository eventoRepository;
    private final LugarRepository lugarRepository;
    private final com.proyecto.sistemasinfor.repository.ReservaRepository reservaRepository;

    public GlobalExceptionHandler(EventoRepository eventoRepository, LugarRepository lugarRepository,
            com.proyecto.sistemasinfor.repository.ReservaRepository reservaRepository) {
        this.eventoRepository = eventoRepository;
        this.lugarRepository = lugarRepository;
        this.reservaRepository = reservaRepository;
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, Model model, WebRequest request) {
        // Log del error para debugging
        ex.printStackTrace();
        
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");
        String message = "Ha ocurrido un error inesperado: " + ex.getMessage();

        // Si la petición es AJAX (por X-Requested-With) o espera JSON (Accept header), devolver JSON
        boolean isAjax = (xRequestedWith != null && "XMLHttpRequest".equalsIgnoreCase(xRequestedWith))
                || (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE));

        if (isAjax) {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("success", false);
            body.put("message", message);
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(body);
        }

        // Obtener URI para decidir qué vista retornar
        String path = null;
        if (request instanceof ServletWebRequest) {
            HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
            path = servletReq.getRequestURI();
            HttpSession session = servletReq.getSession(false);

            // Si la petición era para /perfil o /auth/perfil, intentar devolver la vista perfil con usuario en modelo
            if (path != null && (path.startsWith("/perfil") || path.startsWith("/auth/perfil"))) {
                if (session == null || session.getAttribute("usuario") == null) {
                    return "redirect:/auth/login";
                }
                model.addAttribute("usuario", session.getAttribute("usuario"));
                model.addAttribute("error", message);
                return "perfil";
            }
        }

        // Si la petición estaba relacionada con admin/eventos devolver la vista de eventos con datos
        if (path != null && path.startsWith("/admin/eventos")) {
            model.addAttribute("error", message);
            try {
                model.addAttribute("eventos", eventoRepository.findAllByOrderByFechaAsc());
            } catch (Exception e) {
                model.addAttribute("eventos", java.util.Collections.emptyList());
            }
            try {
                model.addAttribute("lugares", lugarRepository.findAll());
            } catch (Exception e) {
                model.addAttribute("lugares", java.util.Collections.emptyList());
            }
            return "admin-eventos";
        }

        // Si la petición estaba relacionada con admin/reservas devolver la vista con datos
        if (path != null && path.startsWith("/admin/reservas")) {
            model.addAttribute("error", message);
            try {
                model.addAttribute("reservasPendientes", reservaRepository.findByEstadoOrderByFechaCreacionAsc(
                        com.proyecto.sistemasinfor.model.Reserva.EstadoReserva.PENDIENTE));
            } catch (Exception e) {
                model.addAttribute("reservasPendientes", java.util.Collections.emptyList());
            }
            try {
                model.addAttribute("todasReservas", reservaRepository.findAll());
            } catch (Exception e) {
                model.addAttribute("todasReservas", java.util.Collections.emptyList());
            }
            return "admin-reservas";
        }

        // Si la petición estaba relacionada con /reservas (usuario normal) devolver la vista con datos
        if (path != null && path.startsWith("/reservas")) {
            model.addAttribute("error", message);
            try {
                if (request instanceof ServletWebRequest) {
                    HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
                    HttpSession session = servletReq.getSession(false);
                    if (session != null && session.getAttribute("usuario") != null) {
                        com.proyecto.sistemasinfor.model.User usuario = (com.proyecto.sistemasinfor.model.User) session.getAttribute("usuario");
                        model.addAttribute("reservas", reservaRepository.findByUsuarioOrderByFechaDescFechaCreacionDesc(usuario));
                        model.addAttribute("lugares", lugarRepository.findAll());
                        model.addAttribute("nuevaReserva", new com.proyecto.sistemasinfor.model.Reserva());
                        return "reservas";
                    }
                }
            } catch (Exception e) {
                model.addAttribute("reservas", java.util.Collections.emptyList());
                model.addAttribute("lugares", java.util.Collections.emptyList());
            }
            return "reservas";
        }

        // Fallback: devolver una vista de error genérica
        model.addAttribute("error", message);
        return "error";
    }
}
