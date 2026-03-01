package com.mazasoft.ecommerce.userservice.dto.event;

import com.mazasoft.ecommerce.userservice.enums.EventType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProvisionRequestedPayload(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        String realm,
        UUID externalId,
        UserData user,
        List<String> roles,
        String idempotencyKey
) {

    public record UserData(
            String username,
            String email,
            String firstName,
            String lastName,
            boolean enabled
    ) {}
}