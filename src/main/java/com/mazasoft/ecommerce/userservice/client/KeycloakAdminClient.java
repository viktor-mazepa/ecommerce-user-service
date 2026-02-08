package com.mazasoft.ecommerce.userservice.client;

import com.mazasoft.ecommerce.userservice.providers.KeycloakAdminTokenProvider;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort.CreateUserCommand;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort.UpdateUserCommand;
import com.mazasoft.ecommerce.userservice.ports.IdentityAdminPort.RoleRepresentation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.*;

public class KeycloakAdminClient implements IdentityAdminPort {
    private final RestClient restClient;
    private final KeycloakAdminTokenProvider tokenProvider;
    private final String adminBaseUrl; // .../admin/realms/{realm}

    public KeycloakAdminClient(RestClient restClient, KeycloakAdminTokenProvider tokenProvider, String adminBaseUrl) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
        this.adminBaseUrl = adminBaseUrl;
    }

    public String createUser(CreateUserCommand cmd) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", cmd.userName());
        payload.put("email", cmd.email());
        if (cmd.firstName() != null) {
            payload.put("firstName", cmd.firstName());
        }
        if (cmd.lastName() != null) {
            payload.put("lastName", cmd.lastName());
        }
        payload.put("enabled", true);
        payload.put("emailVerified", false);

        var response = restClient.post()
                .uri(adminBaseUrl + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Keycloak did not return Location header for created user");
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    public void setEnabled(UUID kcUserId, boolean enabled) {
        Map<String, Object> payload = Map.of("enabled", enabled);

        restClient.put()
                .uri(adminBaseUrl + "/users/{id}", kcUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteUser(UUID kcUserId) {
        restClient.delete()
                .uri(adminBaseUrl + "/users/{id}", kcUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .retrieve()
                .toBodilessEntity();
    }

    public void updateUser(UUID kcUserId, UpdateUserCommand cmd) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (cmd.email() != null) {
            payload.put("email", cmd.email());
        }
        if (cmd.firstName() != null) {
            payload.put("firstName", cmd.firstName());
        }
        if (cmd.lastName() != null) {
            payload.put("lastName", cmd.lastName());
        }

        restClient.put()
                .uri(adminBaseUrl + "/users/{id}", kcUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    public RoleRepresentation getRealmRoleByName(String roleName) {
        return restClient.get()
                .uri(adminBaseUrl + "/roles/{roleName}", roleName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RoleRepresentation.class);
    }

    public void assignRealmRoles(String kcUserId, List<RoleRepresentation> roles) {
        if (roles == null || roles.isEmpty()) return;

        restClient.post()
                .uri(adminBaseUrl + "/users/{id}/role-mappings/realm", kcUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(roles)
                .retrieve()
                .toBodilessEntity();
    }

    public List<RoleRepresentation> getUserRealmRoles(UUID kcUserId) {
        RoleRepresentation[] roles = restClient.get()
                .uri(adminBaseUrl + "/users/{id}/role-mappings/realm", kcUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RoleRepresentation[].class);
        if (roles == null || roles.length == 0) {
            return List.of();
        }
        return Arrays.asList(roles);
    }

    public void removeRealmRoles(UUID kcUserId) {
        restClient.delete()
                .uri(adminBaseUrl + "/users/{id}/role-mappings/realm", kcUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getBearerToken())
                .retrieve()
                .toBodilessEntity();
    }

    public void replaceRealmRoles(UUID kcUserId, List<RoleRepresentation> desiredRoles) {
        List<RoleRepresentation> currentRoles = getUserRealmRoles(kcUserId);
        List<RoleRepresentation> toAdd = desiredRoles == null ? List.of() : desiredRoles.stream()
                .filter(desired -> currentRoles.stream().noneMatch(current -> roleMatches(current, desired)))
                .toList();

        removeRealmRoles(kcUserId);
        assignRealmRoles(kcUserId.toString(), toAdd);
    }

    private boolean roleMatches(RoleRepresentation a, RoleRepresentation b) {
        if (a == null || b == null) return false;
        if (a.id() != null && b.id() != null) {
            return a.id().equals(b.id());
        }
        return a.name() != null && a.name().equals(b.name());
    }

}
