package com.proyecto.sistemasinfor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.Map;
import java.util.HashMap;
import com.proyecto.sistemasinfor.service.LugarService;
import com.proyecto.sistemasinfor.model.Lugar;
import org.springframework.beans.factory.annotation.Autowired;
import com.proyecto.sistemasinfor.service.HuggingFaceService;

@Controller
public class ChatbotController {

    @Autowired
    private LugarService lugarService;

    @Autowired
    private HuggingFaceService huggingFaceService;

    @GetMapping("/chatbot")
    public String chatbotPage(Model model) {
        return "chatbot";
    }

    @PostMapping("/chatbot/message")
    @ResponseBody
    public Map<String, String> chatbotMessage(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String reply = getBotReply(userMessage);
        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return response;
    }

    private String getBotReply(String message) {
        message = message.toLowerCase();

        // Preguntas sobre lugares
        if (message.contains("gimnasio") || message.contains("biblioteca") || message.contains("sala de estudio")) {
            String lugarBuscado = null;
            if (message.contains("gimnasio")) lugarBuscado = "gimnasio";
            else if (message.contains("biblioteca")) lugarBuscado = "biblioteca";
            else if (message.contains("sala de estudio")) lugarBuscado = "sala de estudio";

            if (lugarBuscado != null) {
                var lugarOpt = lugarService.buscarPorNombre(lugarBuscado);
                if (lugarOpt.isPresent()) {
                    Lugar lugar = lugarOpt.get();
                    return "El estado actual del " + lugar.getNombre() + " es: " + lugar.getEstado() +
                        ". Horario: " + lugar.getHorario();
                } else {
                    return "No tengo información actualizada sobre " + lugarBuscado + ".";
                }
            }
        }

        // Otras respuestas frecuentes
        if (message.contains("eventos") || message.contains("actividades")) {
            return "Puedes consultar los próximos eventos de bienestar en el portal institucional.";
        }
        if (message.contains("reserva") || message.contains("cupo")) {
            return "Para reservar un espacio, indícanos cuál deseas y la fecha.";
        }

        // Si no hay respuesta local, guía al usuario sobre cómo preguntar
        return "No entendí tu pregunta. 🤔\n\n" +
               "Puedo ayudarte con:\n" +
               "• 'estado del gimnasio' - Ver si está abierto\n" +
               "• 'horario de la biblioteca' - Consultar horarios\n" +
               "• 'sala de estudio' - Información de salas\n" +
               "• 'eventos' o 'actividades' - Próximos eventos\n" +
               "• 'reserva' o 'cupo' - Cómo hacer reservas\n\n" +
               "Intenta usar estas palabras clave en tu pregunta.";
    }
}