package com.giacomelli.RBAC_OAuth2.Config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth0")
public record Auth0Properties(
        @NotBlank String domain,
        @NotBlank String connection,
        @NotNull @Valid M2m m2m,
        @NotNull @Valid Application application
) {

    public record M2m(
            @NotBlank String clientId,
            @NotBlank String clientSecret
    ) {
    }

    public record Application(
            @NotBlank String clientId
    ) {
    }
}
