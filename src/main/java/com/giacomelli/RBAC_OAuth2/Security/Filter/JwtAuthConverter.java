package com.giacomelli.RBAC_OAuth2.Security.Filter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter(); // -> Servirá para extraer los permisos estándar (como los scopes de OAuth2) si el token los trae
    private final String ROLES_CLAIM_NAME = "https://labortrack.com/roles"; // Es la clave (Claim Name) del JSON en el JWT donde Auth0 adjuntará los roles

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        /*
            jwt.getClaimAsStringList(...): Busca dentro del JSON del JWT la propiedad
            "[https://labortrack.com/roles](https://labortrack.com/roles)".
            Si existe, la parsea como un arreglo de cadenas (ejemplo: ["admin", "jefe_de_obra"]).
         */
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM_NAME);

        if (roles != null) {
            List<SimpleGrantedAuthority> roleAuthorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .toList();
            authorities.addAll(roleAuthorities);
        }

        return authorities;
    }
}
