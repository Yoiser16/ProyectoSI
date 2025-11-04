package com.proyecto.sistemasinfor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.sistemasinfor.model.Setting;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByK(String k);
}
