package com.giacomelli.RBAC_OAuth2.Config;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Auth0Properties.class)
public class Auth0Configuration {

    @Bean
    ManagementApi managementApi(Auth0Properties properties) {
        return ManagementApi.builder()
                .domain(properties.domain())
                .clientCredentials(
                        properties.m2m().clientId(),
                        properties.m2m().clientSecret()
                )
                .build();
    }

    @Bean
    AuthAPI authApi(Auth0Properties properties) {
        return AuthAPI.newBuilder(
                        properties.domain(),
                        properties.application().clientId()
                )
                .build();
    }
}
