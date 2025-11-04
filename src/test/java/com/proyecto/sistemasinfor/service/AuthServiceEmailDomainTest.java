package com.proyecto.sistemasinfor.service;

import com.proyecto.sistemasinfor.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthServiceEmailDomainTest {

    @Test
    void student_allows_miremington_and_public_providers() {
        AuthService svc = new AuthService();
        // miremington permitido para estudiante
        Assertions.assertTrue(svc.isAllowedEmailForRole("juan@miremington.edu.co", Role.STUDENT));
        // proveedores públicos permitidos
        Assertions.assertTrue(svc.isAllowedEmailForRole("ana@gmail.com", Role.STUDENT));
        Assertions.assertTrue(svc.isAllowedEmailForRole("pepe@outlook.com", Role.STUDENT));
        // uniremington NO permitido para estudiante
        Assertions.assertFalse(svc.isAllowedEmailForRole("maria@uniremington.edu.co", Role.STUDENT));
    }

    @Test
    void admin_allows_uniremington_and_public_providers() {
        AuthService svc = new AuthService();
        // uniremington permitido para otros roles
        Assertions.assertTrue(svc.isAllowedEmailForRole("admin@uniremington.edu.co", Role.ADMIN_TI));
        Assertions.assertTrue(svc.isAllowedEmailForRole("espacios@uniremington.edu.co", Role.ADMIN_ESPACIOS));
        // proveedores públicos permitidos
        Assertions.assertTrue(svc.isAllowedEmailForRole("root@hotmail.com", Role.ADMIN_TI));
        // miremington NO permitido para otros roles
        Assertions.assertFalse(svc.isAllowedEmailForRole("alumno@miremington.edu.co", Role.ADMIN_TI));
    }
}
