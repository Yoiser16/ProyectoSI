package com.proyecto.sistemasinfor.service;

import com.proyecto.sistemasinfor.model.Lugar;
import com.proyecto.sistemasinfor.repository.LugarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
public class LugarService {
    @Autowired
    private LugarRepository lugarRepository;

    public Optional<Lugar> buscarPorNombre(String nombre) {
        return lugarRepository.findByNombreIgnoreCase(nombre);
    }

    public List<Lugar> obtenerTodosLosLugares() {
        return lugarRepository.findAll();
    }

    public Lugar guardarLugar(Lugar lugar) {
        return lugarRepository.save(lugar);
    }

    /**
     * Verifica si un lugar debe estar abierto en este momento
     * 
     * @param lugar      El lugar a verificar
     * @param horaActual La hora actual
     * @param diaActual  El día actual de la semana
     * @return true si debe estar abierto, false si debe estar cerrado
     */
    public boolean debeEstarAbierto(Lugar lugar, LocalTime horaActual, DayOfWeek diaActual) {
        // Si no tiene horarios configurados, no hacer cambios automáticos
        if (lugar.getHoraApertura() == null || lugar.getHoraCierre() == null) {
            return false;
        }

        // Verificar si opera hoy
        if (!operaHoy(lugar, diaActual)) {
            return false;
        }

        // Verificar si está dentro del horario
        LocalTime apertura = lugar.getHoraApertura();
        LocalTime cierre = lugar.getHoraCierre();

        // Caso normal: apertura < cierre (ej: 10:00 - 16:00)
        if (apertura.isBefore(cierre)) {
            return !horaActual.isBefore(apertura) && horaActual.isBefore(cierre);
        }
        // Caso horario nocturno: apertura > cierre (ej: 22:00 - 02:00)
        else {
            return !horaActual.isBefore(apertura) || horaActual.isBefore(cierre);
        }
    }

    /**
     * Verifica si el lugar opera en el día especificado (método público para
     * scheduler)
     */
    public boolean operaEnDia(Lugar lugar, DayOfWeek dia) {
        return operaHoy(lugar, dia);
    }

    /**
     * Verifica si el lugar opera en el día especificado
     */
    private boolean operaHoy(Lugar lugar, DayOfWeek dia) {
        String diasOperacion = lugar.getDiasOperacion();
        if (diasOperacion == null || diasOperacion.isEmpty()) {
            // Por defecto opera de lunes a viernes
            return dia.getValue() >= 1 && dia.getValue() <= 5;
        }

        // Convertir DayOfWeek (1=Monday, 7=Sunday) a índice (0=Lunes, 6=Domingo)
        int indice = dia.getValue() - 1;

        String[] dias = diasOperacion.split(",");
        if (indice < dias.length) {
            return "1".equals(dias[indice].trim());
        }

        return false;
    }
}