package com.mazasoft.ecommerce.userservice.jwt;


import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public final class KeycloakJwtAuthConverter {

    private KeycloakJwtAuthConverter() {}

    public static Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = extractRealmRoles(jwt);

            String principal = Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                    .orElse(Optional.ofNullable(jwt.getClaimAsString("email"))
                            .orElse(jwt.getSubject()));

            return new JwtAuthenticationToken(jwt, authorities, principal);
        };
    }

    private static Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return List.of();

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) return List.of();

        return roles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
