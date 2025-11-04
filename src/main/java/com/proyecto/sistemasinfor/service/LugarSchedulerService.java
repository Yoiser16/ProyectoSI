package com.proyecto.sistemasinfor.service;

import com.proyecto.sistemasinfor.model.Lugar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que actualiza automáticamente el estado de los lugares
 * según sus horarios de apertura y cierre
 */
@Service
public class LugarSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(LugarSchedulerService.class);

    @Autowired
    private LugarService lugarService;

    /**
     * Se ejecuta cada minuto para verificar y actualizar el estado de los lugares
     * Cron: cada minuto, todos los días
     */
    @Scheduled(cron = "0 * * * * *")
    public void actualizarEstadoLugares() {
        logger.debug("Verificando estado de lugares...");

        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();
        DayOfWeek diaActual = ahora.getDayOfWeek();

        List<Lugar> lugares = lugarService.obtenerTodosLosLugares();

        for (Lugar lugar : lugares) {
            // Solo actualizar lugares con horarios configurados
            if (lugar.getHoraApertura() == null || lugar.getHoraCierre() == null) {
                continue;
            }

            String estadoActual = lugar.getEstado();

            // Solo actualizar automáticamente lugares que estén en "Abierto" o "Cerrado"
            // No tocar lugares en "Mantenimiento" o "Reservado"
            if (!estadoActual.equalsIgnoreCase("Abierto") &&
                    !estadoActual.equalsIgnoreCase("Cerrado")) {
                logger.debug("⏭️ Omitiendo '{}' - Estado manual: {}", lugar.getNombre(), estadoActual);
                continue;
            }

            boolean debeEstarAbierto = lugarService.debeEstarAbierto(lugar, horaActual, diaActual);
            String nuevoEstado = debeEstarAbierto ? "Abierto" : "Cerrado";

            // Solo actualizar si el estado cambió
            if (!nuevoEstado.equalsIgnoreCase(estadoActual)) {
                // Determinar la razón del cambio para el log
                String razon = determinarRazonCambio(lugar, horaActual, diaActual);

                logger.info("🔄 Actualizando estado de '{}': {} → {} ({})",
                        lugar.getNombre(),
                        estadoActual,
                        nuevoEstado,
                        razon);

                lugar.setEstado(nuevoEstado);
                lugarService.guardarLugar(lugar);
            }
        }
    }

    /**
     * Determina la razón del cambio de estado para logging informativo
     */
    private String determinarRazonCambio(Lugar lugar, LocalTime horaActual, DayOfWeek diaActual) {
        // Verificar si opera hoy
        boolean operaHoy = lugarService.operaEnDia(lugar, diaActual);

        if (!operaHoy) {
            return "No opera los " + getDiaNombre(diaActual);
        }

        LocalTime apertura = lugar.getHoraApertura();
        LocalTime cierre = lugar.getHoraCierre();

        // Verificar si está fuera del horario
        if (horaActual.isBefore(apertura)) {
            return "Antes de hora de apertura (" + apertura + ")";
        } else if (!horaActual.isBefore(cierre)) {
            return "Después de hora de cierre (" + cierre + ")";
        } else if (horaActual.equals(apertura) || horaActual.isAfter(apertura)) {
            return "Hora de apertura alcanzada (" + apertura + ")";
        }

        return "Cambio de horario";
    }

    /**
     * Obtiene el nombre del día en español
     */
    private String getDiaNombre(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "lunes";
            case TUESDAY -> "martes";
            case WEDNESDAY -> "miércoles";
            case THURSDAY -> "jueves";
            case FRIDAY -> "viernes";
            case SATURDAY -> "sábados";
            case SUNDAY -> "domingos";
        };
    }

    /**
     * Log informativo al inicio - se ejecuta 10 segundos después de iniciar la app
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void logInicio() {
        logger.info("✅ Servicio de actualización automática de lugares activado");
        logger.info("📅 Los lugares se verificarán cada minuto para actualizar su estado según:");
        logger.info("   • Hora de apertura/cierre configurada");
        logger.info("   • Días de operación seleccionados");
        logger.info("   • Solo se actualizan lugares en estado 'Abierto' o 'Cerrado'");
        logger.info("   • Estados 'Mantenimiento' y 'Reservado' no se modifican automáticamente");
    }
}
