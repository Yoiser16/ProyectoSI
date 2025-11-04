package com.proyecto.sistemasinfor.controller;

import com.proyecto.sistemasinfor.model.Lugar;
import com.proyecto.sistemasinfor.model.Role;
import com.proyecto.sistemasinfor.repository.LugarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/lugares")
public class AdminLugarController {

    @Autowired
    private LugarRepository lugarRepository;

    private boolean tienePermiso(HttpSession session) {
        Object rol = session.getAttribute("rol");
        return rol == Role.ADMIN_ESPACIOS || rol == Role.ADMIN_TI;
    }

    @GetMapping
    public String listarLugares(HttpSession session, Model model) {
        if (!tienePermiso(session))
            return "redirect:/menu";

        // Crear lugares de ejemplo si no existen
        if (lugarRepository.count() == 0) {
            crearLugaresEjemplo();
        }

        model.addAttribute("lugares", lugarRepository.findAll());
        return "admin-lugares";
    }

    @GetMapping("/editar/{id}")
    public String editarLugar(@PathVariable Long id, HttpSession session, Model model) {
        if (!tienePermiso(session))
            return "redirect:/menu";
        Lugar lugar = lugarRepository.findById(id).orElseThrow();
        model.addAttribute("lugar", lugar);
        return "editar-lugar";
    }

    @PostMapping("/guardar")
    public String guardarLugar(@ModelAttribute Lugar lugar,
            @RequestParam(required = false) String horaAperturaStr,
            @RequestParam(required = false) String horaCierreStr,
            HttpSession session) {
        if (!tienePermiso(session))
            return "redirect:/menu";

        // Parsear las horas si fueron proporcionadas
        if (horaAperturaStr != null && !horaAperturaStr.isEmpty()) {
            lugar.setHoraApertura(LocalTime.parse(horaAperturaStr));
        }
        if (horaCierreStr != null && !horaCierreStr.isEmpty()) {
            lugar.setHoraCierre(LocalTime.parse(horaCierreStr));
        }

        lugarRepository.save(lugar);
        return "redirect:/admin/lugares";
    }

    @GetMapping("/nuevo")
    public String nuevoLugar(HttpSession session, Model model) {
        if (!tienePermiso(session))
            return "redirect:/menu";
        model.addAttribute("lugar", new Lugar());
        return "editar-lugar";
    }

    private void crearLugaresEjemplo() {
        Lugar gimnasio = new Lugar();
        gimnasio.setNombre("Gimnasio");
        gimnasio.setHorario("6:00 AM - 9:00 PM, Lunes a Sábado");
        gimnasio.setEstado("Abierto");
        gimnasio.setCapacidad(50);
        gimnasio.setDescripcion("Gimnasio universitario equipado");
        gimnasio.setHoraApertura(LocalTime.of(6, 0)); // 6:00 AM
        gimnasio.setHoraCierre(LocalTime.of(21, 0)); // 9:00 PM
        gimnasio.setDiasOperacion("1,1,1,1,1,1,0"); // Lunes a Sábado
        lugarRepository.save(gimnasio);

        Lugar biblioteca = new Lugar();
        biblioteca.setNombre("Biblioteca Central");
        biblioteca.setHorario("7:00 AM - 8:00 PM, Lunes a Viernes");
        biblioteca.setEstado("Abierto");
        biblioteca.setCapacidad(200);
        biblioteca.setDescripcion("Biblioteca principal del campus");
        biblioteca.setHoraApertura(LocalTime.of(7, 0)); // 7:00 AM
        biblioteca.setHoraCierre(LocalTime.of(20, 0)); // 8:00 PM
        biblioteca.setDiasOperacion("1,1,1,1,1,0,0"); // Lunes a Viernes
        lugarRepository.save(biblioteca);

        Lugar salaEstudio = new Lugar();
        salaEstudio.setNombre("Sala de Estudio Grupal");
        salaEstudio.setHorario("8:00 AM - 6:00 PM, Lunes a Viernes");
        salaEstudio.setEstado("Abierto");
        salaEstudio.setCapacidad(12);
        salaEstudio.setDescripcion("Sala para trabajo en equipo");
        salaEstudio.setHoraApertura(LocalTime.of(8, 0)); // 8:00 AM
        salaEstudio.setHoraCierre(LocalTime.of(18, 0)); // 6:00 PM
        salaEstudio.setDiasOperacion("1,1,1,1,1,0,0"); // Lunes a Viernes
        lugarRepository.save(salaEstudio);

        Lugar laboratorio = new Lugar();
        laboratorio.setNombre("Laboratorio de Informática");
        laboratorio.setHorario("9:00 AM - 5:00 PM, Lunes a Viernes");
        laboratorio.setEstado("Mantenimiento");
        laboratorio.setCapacidad(30);
        laboratorio.setDescripcion("Laboratorio con equipos de cómputo");
        laboratorio.setHoraApertura(LocalTime.of(9, 0)); // 9:00 AM
        laboratorio.setHoraCierre(LocalTime.of(17, 0)); // 5:00 PM
        laboratorio.setDiasOperacion("1,1,1,1,1,0,0"); // Lunes a Viernes
        lugarRepository.save(laboratorio);
    }

    // Método AJAX para crear lugares desde otras vistas
    @PostMapping(value = "/crear-ajax", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearLugarAjax(@RequestParam String nombre,
            @RequestParam Integer capacidad,
            @RequestParam(required = false) String descripcion,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!tienePermiso(session)) {
            response.put("success", false);
            response.put("message", "No tienes permisos para crear lugares");
            return ResponseEntity.ok(response);
        }

        try {
            Lugar lugar = new Lugar();
            lugar.setNombre(nombre);
            lugar.setCapacidad(capacidad);
            lugar.setDescripcion(descripcion != null ? descripcion : "");
            lugar.setHorario("8:00 AM - 6:00 PM");
            lugar.setEstado("abierto");

            Lugar lugarGuardado = lugarRepository.save(lugar);

            Map<String, Object> lugarData = new HashMap<>();
            lugarData.put("id", lugarGuardado.getId());
            lugarData.put("nombre", lugarGuardado.getNombre());
            lugarData.put("capacidad", lugarGuardado.getCapacidad());

            response.put("success", true);
            response.put("lugar", lugarData);
            response.put("message", "Lugar creado exitosamente");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al crear el lugar: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}