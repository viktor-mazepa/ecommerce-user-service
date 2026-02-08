package com.mazasoft.ecommerce.userservice.ports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IdentityAdminPort {

    String createUser(CreateUserCommand cmd);

    void updateUser(UUID kcUserId, UpdateUserCommand cmd);

    void setEnabled(UUID kcUserId, boolean enabled);

    void deleteUser(UUID kcUserId);

    RoleRepresentation getRealmRoleByName(String roleName);

    void assignRealmRoles(String kcUserId, List<RoleRepresentation> roles);

    void replaceRealmRoles(UUID kcUserId, List<RoleRepresentation> desiredRoles);

    record CreateUserCommand(
            String userName,
            String email,
            String avatar,
            String firstName,
            String lastName,
            LocalDate birthDate,
            String phoneNumber
    ) {
    }

    record UpdateUserCommand(
            String email,
            String firstName,
            String lastName
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RoleRepresentation(String id, String name) { }
}
