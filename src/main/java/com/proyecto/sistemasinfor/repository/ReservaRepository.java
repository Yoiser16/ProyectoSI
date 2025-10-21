package com.proyecto.sistemasinfor.repository;

import com.proyecto.sistemasinfor.model.Reserva;
import com.proyecto.sistemasinfor.model.User;
import com.proyecto.sistemasinfor.model.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // Encontrar reservas por usuario ordenadas por fecha y fecha de creación
    @Query("SELECT r FROM Reserva r WHERE r.usuario = :usuario ORDER BY r.fecha DESC, r.fechaCreacion DESC")
    List<Reserva> findByUsuarioOrderByFechaDescFechaCreacionDesc(@Param("usuario") User usuario);
    
    // Encontrar reservas por estado (ascendente o descendente)
    List<Reserva> findByEstadoOrderByFechaCreacionDesc(Reserva.EstadoReserva estado);
    List<Reserva> findByEstadoOrderByFechaCreacionAsc(Reserva.EstadoReserva estado);
    
    // Verificar conflictos de horario para un lugar en una fecha específica
    @Query("SELECT r FROM Reserva r WHERE r.lugar = :lugar AND r.fecha = :fecha " +
        "AND (r.estado = :estadoPendiente OR r.estado = :estadoAprobada) " +
           "AND ((r.horaInicio <= :horaInicio AND r.horaFin > :horaInicio) " +
           "OR (r.horaInicio < :horaFin AND r.horaFin >= :horaFin) " +
           "OR (r.horaInicio >= :horaInicio AND r.horaFin <= :horaFin))")
    List<Reserva> findConflictingReservations(@Param("lugar") Lugar lugar, 
                           @Param("fecha") LocalDate fecha,
                           @Param("horaInicio") LocalTime horaInicio, 
                           @Param("horaFin") LocalTime horaFin,
                           @Param("estadoPendiente") Reserva.EstadoReserva estadoPendiente,
                           @Param("estadoAprobada") Reserva.EstadoReserva estadoAprobada);
    
    // Encontrar reservas de un lugar específico ordenadas por fecha descendente y hora de inicio ascendente
    @Query("SELECT r FROM Reserva r WHERE r.lugar = :lugar ORDER BY r.fecha DESC, r.horaInicio ASC")
    List<Reserva> findByLugarOrderByFechaDescHoraInicioAsc(@Param("lugar") Lugar lugar);
    
    // Reservas aprobadas para un lugar en una fecha específica
    @Query("SELECT r FROM Reserva r WHERE r.lugar = :lugar AND r.fecha = :fecha AND r.estado = :estado ORDER BY r.horaInicio")
    List<Reserva> findApprovedReservationsByLugarAndFecha(@Param("lugar") Lugar lugar, @Param("fecha") LocalDate fecha, @Param("estado") Reserva.EstadoReserva estado);
}