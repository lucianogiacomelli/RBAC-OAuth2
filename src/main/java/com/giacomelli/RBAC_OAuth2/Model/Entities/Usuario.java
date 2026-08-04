package com.giacomelli.RBAC_OAuth2.Model.Entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "User")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario extends Base{
    @Column(name = "auth0_id", unique = true, nullable = false)
    private String authId;
    @Column(unique = true, nullable = true)
    private String email;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String apellido;
}
