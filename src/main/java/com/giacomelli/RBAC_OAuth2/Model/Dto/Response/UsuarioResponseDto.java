package com.giacomelli.RBAC_OAuth2.Model.Dto.Response;

public record UsuarioResponseDto(
        Long id, String auth0Id, String email,
        String nombre, String apellido
) {
}
