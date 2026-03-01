package com.mazasoft.ecommerce.userservice.utils;

import com.mazasoft.ecommerce.userservice.dto.CreateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.UpdateUserAdmin;
import com.mazasoft.ecommerce.userservice.dto.event.UserProvisionRequestedPayload;
import com.mazasoft.ecommerce.userservice.entities.User;
import com.mazasoft.ecommerce.userservice.enums.EventType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class EventPayloadGenerator {

    private static final String REALM = "ecommerce";
    private static final boolean USER_ENABLED = true;

    public UserProvisionRequestedPayload generateCreateUserPayload(UUID userId, CreateUserAdmin request) {
        return buildPayload(
                userId,
                request.userName(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.role(),
                USER_ENABLED,
                EventType.USER_CREATE
        );
    }

    public UserProvisionRequestedPayload generateUpdateUserPayload(UUID userId, UpdateUserAdmin request) {
        return buildPayload(
                userId,
                request.userName(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.role(),
                USER_ENABLED,
                EventType.USER_UPDATE
        );
    }

    public UserProvisionRequestedPayload generateDeleteUserPayload(User user) {
        return buildPayload(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFistName(),
                user.getLastName(),
                user.getRole(),
                false,
                EventType.USER_DELETE
        );
    }

    public UserProvisionRequestedPayload generateEnableUserPayload(User user) {
        return buildPayload(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFistName(),
                user.getLastName(),
                user.getRole(),
                false,
                EventType.USER_ENABLE
        );
    }

    public UserProvisionRequestedPayload generateDisableUserPayload(User user) {
        return buildPayload(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFistName(),
                user.getLastName(),
                user.getRole(),
                false,
                EventType.USER_DISABLE
        );
    }

    private UserProvisionRequestedPayload buildPayload(
            UUID userId,
            String username,
            String email,
            String firstName,
            String lastName,
            String role,
            boolean enabled,
            EventType eventType
    ) {
        return new UserProvisionRequestedPayload(
                UUID.randomUUID(),
                eventType,
                Instant.now(),
                REALM,
                userId,
                new UserProvisionRequestedPayload.UserData(
                        username,
                        email,
                        firstName,
                        lastName,
                        enabled
                ),
                buildRoles(role),
                eventType + ":" + userId
        );
    }

    private List<String> buildRoles(String role) {
        if (role == null || role.isBlank()) {
            return new ArrayList<>();
        }
        return Collections.singletonList(role);
    }
}
