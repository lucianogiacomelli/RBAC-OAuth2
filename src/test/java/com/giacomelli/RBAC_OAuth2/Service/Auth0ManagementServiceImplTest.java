package com.giacomelli.RBAC_OAuth2.Service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.RolesClient;
import com.auth0.client.mgmt.UsersClient;
import com.auth0.client.mgmt.core.SyncPagingIterable;
import com.auth0.client.mgmt.types.CreateUserRequestContent;
import com.auth0.client.mgmt.types.CreateUserResponseContent;
import com.auth0.client.mgmt.types.ListRolesRequestParameters;
import com.auth0.client.mgmt.types.Role;
import com.auth0.exception.Auth0Exception;
import com.auth0.net.Request;
import com.giacomelli.RBAC_OAuth2.Config.Auth0Properties;
import com.giacomelli.RBAC_OAuth2.Model.Dto.Request.UsuarioRequestDto;
import com.giacomelli.RBAC_OAuth2.Model.Entities.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Auth0ManagementServiceImplTest {

    private static final UsuarioRequestDto USUARIO = new UsuarioRequestDto(
            "ana@example.com", "Ana", "Pérez", Roles.ADMIN
    );

    @Mock
    private ManagementApi managementApi;
    @Mock
    private AuthAPI authApi;
    @Mock
    private RolesClient rolesClient;
    @Mock
    private UsersClient usersClient;
    @Mock
    private com.auth0.client.mgmt.users.RolesClient userRolesClient;
    @Mock
    private SyncPagingIterable<Role> roles;
    @Mock
    private Request<Void> resetPasswordRequest;

    private Auth0ManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        Auth0Properties properties = new Auth0Properties(
                "dev-example.us.auth0.com",
                "Username-Password-Authentication",
                new Auth0Properties.M2m("m2m-client-id", "m2m-client-secret"),
                new Auth0Properties.Application("application-client-id")
        );
        service = new Auth0ManagementServiceImpl(managementApi, authApi, properties);

        when(managementApi.roles()).thenReturn(rolesClient);
        when(rolesClient.list(any(ListRolesRequestParameters.class))).thenReturn(roles);
    }

    @Test
    void creaElUsuarioAsignaElRolYSolicitaElCorreoDeContrasena() throws Exception {
        Role adminRole = Role.builder().id("rol_admin").name("ADMIN").build();
        when(roles.streamItems()).thenReturn(Stream.of(adminRole));
        when(managementApi.users()).thenReturn(usersClient);
        when(usersClient.create(any())).thenReturn(
                CreateUserResponseContent.builder().userId("auth0|123").build()
        );
        when(usersClient.roles()).thenReturn(userRolesClient);
        when(authApi.resetPassword(USUARIO.email(), "Username-Password-Authentication"))
                .thenReturn(resetPasswordRequest);

        String auth0Id = service.crearUsuarioEnAuth0(USUARIO);

        assertThat(auth0Id).isEqualTo("auth0|123");
        ArgumentCaptor<CreateUserRequestContent> userCaptor = ArgumentCaptor.forClass(CreateUserRequestContent.class);
        verify(usersClient).create(userCaptor.capture());
        CreateUserRequestContent createdUser = userCaptor.getValue();
        assertThat(createdUser.getConnection()).isEqualTo("Username-Password-Authentication");
        assertThat(createdUser.getEmail()).contains(USUARIO.email());
        assertThat(createdUser.getPassword()).hasValueSatisfying(password -> {
            assertThat(password).hasSize(32);
            assertThat(password).containsPattern("[A-Z]");
            assertThat(password).containsPattern("[a-z]");
            assertThat(password).containsPattern("[0-9]");
            assertThat(password).containsPattern("[!@#$%^&*\\-_]");
        });
        verify(userRolesClient).assign(eq("auth0|123"), any());
        verify(authApi).resetPassword(USUARIO.email(), "Username-Password-Authentication");
        verify(resetPasswordRequest).execute();
    }

    @Test
    void noCreaElUsuarioCuandoNoExisteElRol() {
        when(roles.streamItems()).thenReturn(Stream.empty());

        assertThatThrownBy(() -> service.crearUsuarioEnAuth0(USUARIO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN");

        verify(usersClient, never()).create(any());
    }

    @Test
    void eliminaElUsuarioSiFallaElEnvioDelCorreo() throws Exception {
        Role adminRole = Role.builder().id("rol_admin").name("ADMIN").build();
        when(roles.streamItems()).thenReturn(Stream.of(adminRole));
        when(managementApi.users()).thenReturn(usersClient);
        when(usersClient.create(any())).thenReturn(
                CreateUserResponseContent.builder().userId("auth0|123").build()
        );
        when(usersClient.roles()).thenReturn(userRolesClient);
        when(authApi.resetPassword(USUARIO.email(), "Username-Password-Authentication"))
                .thenReturn(resetPasswordRequest);
        when(resetPasswordRequest.execute()).thenThrow(new Auth0Exception("No se pudo enviar el correo"));

        assertThatThrownBy(() -> service.crearUsuarioEnAuth0(USUARIO))
                .isInstanceOf(Auth0Exception.class);

        verify(usersClient).delete("auth0|123");
    }
}
