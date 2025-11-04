package com.proyecto.sistemasinfor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_settings")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 120)
    private String k;

    @Column(columnDefinition = "TEXT")
    private String v;

    public Setting() {
    }

    public Setting(String k, String v) {
        this.k = k;
        this.v = v;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }
}
