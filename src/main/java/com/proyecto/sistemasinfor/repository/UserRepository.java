// filepath: d:\Descargas\sistemasinfor\src\main\java\com\proyecto\sistemasinfor\repository\UserRepository.java
package com.proyecto.sistemasinfor.repository;

import com.proyecto.sistemasinfor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}