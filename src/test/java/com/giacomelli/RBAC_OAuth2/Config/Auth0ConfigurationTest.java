package com.giacomelli.RBAC_OAuth2.Config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class Auth0ConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Auth0Configuration.class)
            .withPropertyValues(
                    "auth0.domain=dev-example.us.auth0.com",
                    "auth0.connection=Username-Password-Authentication",
                    "auth0.m2m.client-id=m2m-client-id",
                    "auth0.m2m.client-secret=m2m-client-secret",
                    "auth0.application.client-id=application-client-id"
            );

    @Test
    void registraLosClientesCuandoLaConfiguracionEsValida() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Auth0Properties.class);
            assertThat(context).hasBean("managementApi");
            assertThat(context).hasBean("authApi");
        });
    }

    @Test
    void fallaCuandoFaltaUnaPropiedadObligatoria() {
        new ApplicationContextRunner()
                .withUserConfiguration(Auth0Configuration.class)
                .withPropertyValues(
                        "auth0.domain=dev-example.us.auth0.com",
                        "auth0.connection=Username-Password-Authentication",
                        "auth0.m2m.client-id=m2m-client-id",
                        "auth0.m2m.client-secret=m2m-client-secret"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }
}
