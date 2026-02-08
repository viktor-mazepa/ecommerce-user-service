package com.mazasoft.ecommerce.userservice.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "app.keycloak")
@Validated
public record KeycloakProperties(@NotBlank String baseUrl,
                          @NotBlank String realm,
                          String tokenPath,
                          String logoutPath,
                          String adminBasePath,
                          @NotBlank String loginClientId,
                          @NotBlank String loginClientSecret,
                          @NotBlank String adminClientId,
                          @NotBlank String adminClientSecret) {
    public String tokenUrl() {
        String path = (tokenPath == null || tokenPath.isBlank())
                ? "/realms/{realm}/protocol/openid-connect/token"
                : tokenPath;
        return (baseUrl + path).replace("{realm}", realm);
    }

    public String logoutUrl() {
        String path = (logoutPath == null || logoutPath.isBlank())
                ? "/realms/{realm}/protocol/openid-connect/logout"
                : logoutPath;
        return (baseUrl + path).replace("{realm}", realm);
    }

    public String adminBaseUrl() {
        String path = (adminBasePath == null || adminBasePath.isBlank())
                ? "/admin/realms/{realm}"
                : adminBasePath;
        return (baseUrl + path).replace("{realm}", realm);
    }
}
