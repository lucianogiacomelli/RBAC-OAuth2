package com.giacomelli.RBAC_OAuth2.Service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.types.CreateUserRequestContent;
import com.auth0.client.mgmt.types.ListRolesRequestParameters;
import com.auth0.client.mgmt.types.Role;
import com.auth0.client.mgmt.users.types.AssignUserRolesRequestContent;
import com.auth0.exception.Auth0Exception;
import com.giacomelli.RBAC_OAuth2.Config.Auth0Properties;
import com.giacomelli.RBAC_OAuth2.Model.Dto.Request.UsuarioRequestDto;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class Auth0ManagementServiceImpl implements IAuth0ManagementService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*-_";
    private static final String PASSWORD_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;
    private static final int TEMPORARY_PASSWORD_LENGTH = 32;

    private final ManagementApi managementApi;
    private final AuthAPI authApi;
    private final Auth0Properties properties;

    public Auth0ManagementServiceImpl(
            ManagementApi managementApi,
            AuthAPI authApi,
            Auth0Properties properties
    ) {
        this.managementApi = managementApi;
        this.authApi = authApi;
        this.properties = properties;
    }

    @Override
    public ManagementApi getManagementApi() throws Auth0Exception {
        return managementApi;
    }

    @Override
    public String crearUsuarioEnAuth0(UsuarioRequestDto usuario) throws Auth0Exception {
        Role role = buscarRol(usuario.rol().name()); // PASO 1 --> Buscar en Auth0 si existe el rol solicitado
        String auth0Id = null;

        try {
            auth0Id = managementApi.users()
                    .create(construirUsuario(usuario))
                    .getUserId()
                    .orElseThrow(() -> new IllegalStateException("Auth0 no devolvió el id del usuario creado"));

            String roleId = role.getId()
                    .orElseThrow(() -> new IllegalStateException("El rol de Auth0 no tiene id"));
            managementApi.users().roles().assign(
                    auth0Id,
                    AssignUserRolesRequestContent.builder()
                            .roles(List.of(roleId))
                            .build()
            );

            authApi.resetPassword(usuario.email(), properties.connection()).execute();
            return auth0Id;
        } catch (Auth0Exception exception) {
            eliminarUsuarioSiFueCreado(auth0Id, exception);
            throw exception;
        } catch (RuntimeException exception) {
            eliminarUsuarioSiFueCreado(auth0Id, exception);
            throw exception;
        }
    }

    private Role buscarRol(String nombreRol) {
        return managementApi.roles()
                .list(ListRolesRequestParameters.builder().nameFilter(nombreRol).build())
                .streamItems()
                .filter(role -> role.getName().filter(nombreRol::equals).isPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el rol de Auth0 requerido: " + nombreRol
                ));
    }

    private CreateUserRequestContent construirUsuario(UsuarioRequestDto usuario) {
        return CreateUserRequestContent.builder()
                .connection(properties.connection())
                .email(usuario.email())
                .givenName(usuario.nombre())
                .familyName(usuario.apellido())
                .name(usuario.nombre() + " " + usuario.apellido())
                .emailVerified(false)
                .password(generarContrasenaTemporal())
                .build();
    }



    private void eliminarUsuarioSiFueCreado(String auth0Id, Exception errorOriginal) {
        if (auth0Id == null) {
            return;
        }

        try {
            managementApi.users().delete(auth0Id);
        } catch (RuntimeException cleanupError) {
            errorOriginal.addSuppressed(cleanupError);
        }
    }

    private String generarContrasenaTemporal() {
        char[] password = new char[TEMPORARY_PASSWORD_LENGTH];
        password[0] = caracterAleatorio(UPPERCASE);
        password[1] = caracterAleatorio(LOWERCASE);
        password[2] = caracterAleatorio(DIGITS);
        password[3] = caracterAleatorio(SYMBOLS);

        for (int index = 4; index < password.length; index++) {
            password[index] = caracterAleatorio(PASSWORD_CHARACTERS);
        }

        for (int index = password.length - 1; index > 0; index--) {
            int replacementIndex = SECURE_RANDOM.nextInt(index + 1);
            char temporary = password[index];
            password[index] = password[replacementIndex];
            password[replacementIndex] = temporary;
        }

        return new String(password);
    }

    private char caracterAleatorio(String caracteres) {
        return caracteres.charAt(SECURE_RANDOM.nextInt(caracteres.length()));
    }
}
