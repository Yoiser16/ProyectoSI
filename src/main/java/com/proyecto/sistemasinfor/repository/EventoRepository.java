package com.proyecto.sistemasinfor.repository;

import com.proyecto.sistemasinfor.model.Evento;
import com.proyecto.sistemasinfor.model.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    
    // Encontrar eventos ordenados por fecha
    List<Evento> findAllByOrderByFechaAsc();
    
    // Encontrar eventos en un rango de fechas
    @Query("SELECT e FROM Evento e WHERE e.fecha >= :inicio AND e.fecha <= :fin ORDER BY e.fecha, e.horaInicio")
    List<Evento> findEventosEnRango(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    
    // Encontrar eventos por lugar
    List<Evento> findByLugarOrderByFechaAsc(Lugar lugar);
    
    // Encontrar eventos futuros
    @Query("SELECT e FROM Evento e WHERE e.fecha >= CURRENT_DATE ORDER BY e.fecha, e.horaInicio")
    List<Evento> findEventosFuturos();
    
    // Encontrar eventos de hoy
    @Query("SELECT e FROM Evento e WHERE e.fecha = CURRENT_DATE ORDER BY e.horaInicio")
    List<Evento> findEventosDeHoy();
    
    // Encontrar próximos eventos (siguientes 7 días)
    @Query(value = "SELECT * FROM eventos e WHERE e.fecha >= CURDATE() AND e.fecha <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) ORDER BY e.fecha, e.hora_inicio", nativeQuery = true)
    List<Evento> findProximosEventos();
}