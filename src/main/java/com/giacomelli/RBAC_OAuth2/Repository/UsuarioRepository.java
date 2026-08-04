package com.giacomelli.RBAC_OAuth2.Repository;

import com.giacomelli.RBAC_OAuth2.Model.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findUserByAuth0Id(String auth0Id);
}
