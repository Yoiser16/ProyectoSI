package com.proyecto.sistemasinfor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role rol; // STUDENT, ADMIN_ESPACIOS, ADMIN_TI

    // Campos para control de bloqueo de cuenta
    private Integer failedAttempts = 0;
    private Boolean accountLocked = false;
    private LocalDateTime lockTime;
    private String unlockToken; // Token para desbloquear cuenta

    // Campos para consentimiento y tratamiento de datos (GDPR/LOPD)
    @Column(nullable = true)
    private Boolean privacyPolicyAccepted = false; // Obligatorio - Política de privacidad
    
    @Column(nullable = true)
    private Boolean marketingEmailsAccepted = false; // Opcional - Correos promocionales/recomendaciones
    
    private LocalDateTime consentDate; // Fecha de aceptación del consentimiento

    // Constructor vacío
    public User() {
    }

    // Constructor con campos
    public User(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRol() {
        return rol;
    }

    public void setRol(Role rol) {
        this.rol = rol;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    public String getUnlockToken() {
        return unlockToken;
    }

    public void setUnlockToken(String unlockToken) {
        this.unlockToken = unlockToken;
    }

    public Boolean getPrivacyPolicyAccepted() {
        return privacyPolicyAccepted;
    }

    public void setPrivacyPolicyAccepted(Boolean privacyPolicyAccepted) {
        this.privacyPolicyAccepted = privacyPolicyAccepted;
    }

    public Boolean getMarketingEmailsAccepted() {
        return marketingEmailsAccepted;
    }

    public void setMarketingEmailsAccepted(Boolean marketingEmailsAccepted) {
        this.marketingEmailsAccepted = marketingEmailsAccepted;
    }

    public LocalDateTime getConsentDate() {
        return consentDate;
    }

    public void setConsentDate(LocalDateTime consentDate) {
        this.consentDate = consentDate;
    }
}