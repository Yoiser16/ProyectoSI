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

        // Ayuda general
        if (message.contains("hola") || message.contains("ayuda") || message.contains("qué puedes hacer")) {
            return "👋 ¡Hola! Soy tu asistente virtual de Uniremington.\n\n" +
                    "Puedo ayudarte con:\n\n" +
                    "📍 **Consulta de espacios:**\n" +
                    "   • Estado y horario del gimnasio\n" +
                    "   • Información de la biblioteca\n" +
                    "   • Disponibilidad de salas de estudio\n\n" +
                    "🎉 **Eventos y actividades:**\n" +
                    "   • Próximos eventos de bienestar\n" +
                    "   • Actividades deportivas\n\n" +
                    "📅 **Reservas:**\n" +
                    "   • Cómo hacer una reserva\n" +
                    "   • Consultar disponibilidad\n\n" +
                    "💡 *Escribe tu pregunta de forma natural, por ejemplo:*\n" +
                    "   \"¿Está abierto el gimnasio?\" o \"Horario de la biblioteca\"";
        }

        // Preguntas sobre lugares específicos
        if (message.contains("gimnasio")) {
            if (message.contains("horario") || message.contains("hora") || message.contains("abre")
                    || message.contains("cierra")) {
                var lugarOpt = lugarService.buscarPorNombre("gimnasio");
                if (lugarOpt.isPresent()) {
                    Lugar lugar = lugarOpt.get();
                    return "🏋️ **Gimnasio Institucional**\n\n" +
                            "📅 Horario: " + lugar.getHorario() + "\n" +
                            "📊 Estado actual: " + lugar.getEstado() + "\n\n" +
                            "¿Necesitas hacer una reserva?";
                }
            }
            if (message.contains("estado") || message.contains("abierto") || message.contains("disponible")) {
                var lugarOpt = lugarService.buscarPorNombre("gimnasio");
                if (lugarOpt.isPresent()) {
                    Lugar lugar = lugarOpt.get();
                    String emoji = lugar.getEstado().toLowerCase().contains("abierto") ? "✅" : "🔒";
                    return emoji + " **Gimnasio:** " + lugar.getEstado() + "\n\n" +
                            "Horario de atención: " + lugar.getHorario();
                }
            }
            // Información general del gimnasio
            var lugarOpt = lugarService.buscarPorNombre("gimnasio");
            if (lugarOpt.isPresent()) {
                Lugar lugar = lugarOpt.get();
                return "🏋️ **Gimnasio Institucional**\n\n" +
                        "📊 Estado: " + lugar.getEstado() + "\n" +
                        "📅 Horario: " + lugar.getHorario() + "\n\n" +
                        "¿Quieres saber cómo hacer una reserva?";
            }
        }

        if (message.contains("biblioteca")) {
            var lugarOpt = lugarService.buscarPorNombre("biblioteca");
            if (lugarOpt.isPresent()) {
                Lugar lugar = lugarOpt.get();
                String emoji = lugar.getEstado().toLowerCase().contains("abierto") ? "✅" : "🔒";
                return "📚 **Biblioteca**\n\n" +
                        emoji + " Estado: " + lugar.getEstado() + "\n" +
                        "⏰ Horario: " + lugar.getHorario() + "\n\n" +
                        "Servicios disponibles:\n" +
                        "   • Préstamo de libros\n" +
                        "   • Salas de lectura\n" +
                        "   • Conexión WiFi";
            }
        }

        if (message.contains("sala de estudio") || message.contains("salas de estudio")) {
            var lugarOpt = lugarService.buscarPorNombre("sala de estudio");
            if (lugarOpt.isPresent()) {
                Lugar lugar = lugarOpt.get();
                String emoji = lugar.getEstado().toLowerCase().contains("disponible") ? "✅" : "⚠️";
                return "📖 **Salas de Estudio**\n\n" +
                        emoji + " Estado: " + lugar.getEstado() + "\n" +
                        "⏰ Horario: " + lugar.getHorario() + "\n\n" +
                        "Para reservar una sala, ve al menú principal → Reservas";
            }
        }

        // Eventos y actividades
        if (message.contains("evento") || message.contains("actividad")) {
            return "🎉 **Eventos y Actividades**\n\n" +
                    "Para consultar los próximos eventos:\n" +
                    "1. Ve al menú principal\n" +
                    "2. Selecciona 'Eventos'\n" +
                    "3. Allí verás todos los eventos disponibles\n\n" +
                    "También puedes inscribirte directamente desde allí.";
        }

        // Información sobre reservas
        if (message.contains("reserva") || message.contains("reservar") || message.contains("cupo")) {
            return "📅 **Sistema de Reservas**\n\n" +
                    "Para hacer una reserva:\n" +
                    "1. Ve al menú principal\n" +
                    "2. Haz clic en 'Reservas'\n" +
                    "3. Selecciona el espacio que necesitas\n" +
                    "4. Elige fecha y hora\n" +
                    "5. Confirma tu reserva\n\n" +
                    "Recibirás una confirmación por correo electrónico.";
        }

        // Horarios en general
        if (message.contains("horario") && !message.contains("gimnasio") && !message.contains("biblioteca")) {
            return "⏰ **Horarios Institucionales**\n\n" +
                    "¿Qué horario necesitas consultar?\n\n" +
                    "• Gimnasio\n" +
                    "• Biblioteca\n" +
                    "• Salas de estudio\n\n" +
                    "Escribe el nombre del espacio que te interesa.";
        }

        // Mensaje de ayuda por defecto - más organizado
        return "🤔 No entendí tu pregunta.\n\n" +
                "**Puedo ayudarte con:**\n\n" +
                "📍 **Espacios:**\n" +
                "   • \"¿Está abierto el gimnasio?\"\n" +
                "   • \"Horario de la biblioteca\"\n" +
                "   • \"Salas de estudio disponibles\"\n\n" +
                "🎉 **Eventos:**\n" +
                "   • \"Próximos eventos\"\n" +
                "   • \"Actividades deportivas\"\n\n" +
                "📅 **Reservas:**\n" +
                "   • \"Cómo hacer una reserva\"\n" +
                "   • \"Reservar gimnasio\"\n\n" +
                "💡 Escribe **\"ayuda\"** para ver todas las opciones.";
    }
}