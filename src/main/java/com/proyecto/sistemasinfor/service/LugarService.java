package com.proyecto.sistemasinfor.service;

import com.proyecto.sistemasinfor.model.Lugar;
import com.proyecto.sistemasinfor.repository.LugarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LugarService {
    @Autowired
    private LugarRepository lugarRepository;

    public Optional<Lugar> buscarPorNombre(String nombre) {
        return lugarRepository.findByNombreIgnoreCase(nombre);
    }
}