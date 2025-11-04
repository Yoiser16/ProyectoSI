package com.proyecto.sistemasinfor.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "lugares")
public class Lugar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String horario; // Formato descriptivo: "10:00 AM - 4:00 PM"
    private String estado; // "Abierto", "Cerrado", etc.
    private Integer capacidad;
    private String descripcion;

    // Horarios para control automático (en formato 24h)
    private LocalTime horaApertura; // Ej: 10:00
    private LocalTime horaCierre; // Ej: 16:00

    // Días de operación (formato: L,M,X,J,V,S,D donde 1=opera, 0=no opera)
    // Por defecto "1,1,1,1,1,0,0" = Lunes a Viernes
    private String diasOperacion;

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public String getDiasOperacion() {
        return diasOperacion;
    }

    public void setDiasOperacion(String diasOperacion) {
        this.diasOperacion = diasOperacion;
    }
}