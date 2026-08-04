package com.giacomelli.RBAC_OAuth2.Model.Dto.Request;

import com.giacomelli.RBAC_OAuth2.Model.Entities.Roles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDto(
        @NotBlank(message = "El email es obligatorio") @Email(message = "Formato de email inválido") String email,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotNull(message = "El rol es obligatorio") Roles rol
) {
}
