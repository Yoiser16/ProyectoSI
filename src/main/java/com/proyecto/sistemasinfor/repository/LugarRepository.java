package com.proyecto.sistemasinfor.repository;

import com.proyecto.sistemasinfor.model.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LugarRepository extends JpaRepository<Lugar, Long> {
    Optional<Lugar> findByNombreIgnoreCase(String nombre);
}