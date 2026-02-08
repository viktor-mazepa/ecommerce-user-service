package com.mazasoft.ecommerce.userservice.configuration;

import com.mazasoft.ecommerce.userservice.configuration.properties.KeycloakProperties;
import com.mazasoft.ecommerce.userservice.jwt.KeycloakJwtAuthConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf-> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "WORKER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(KeycloakJwtAuthConverter.jwtAuthenticationConverter()))
                );

        return http.build();
    }
}
