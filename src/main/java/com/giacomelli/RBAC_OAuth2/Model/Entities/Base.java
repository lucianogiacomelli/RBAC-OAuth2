package com.giacomelli.RBAC_OAuth2.Model.Entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Base implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaAlta;
    private LocalDateTime fechaBaja;

    @PrePersist
    public void prePersist(){
        this.fechaAlta = LocalDateTime.now();
    }
}
