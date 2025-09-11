package com.proyecto.sistemasinfor.service;

import com.proyecto.sistemasinfor.dto.LoginRequest;
import com.proyecto.sistemasinfor.dto.RegisterRequest;
import com.proyecto.sistemasinfor.dto.PasswordResetRequest;
import com.proyecto.sistemasinfor.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> login(LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public User register(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getNombre(), request.getEmail(), encodedPassword);
        return userService.saveUser(user);
    }

    public boolean resetPassword(PasswordResetRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.saveUser(user);
            return true;
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }
}
