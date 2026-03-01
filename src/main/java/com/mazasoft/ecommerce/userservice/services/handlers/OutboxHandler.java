package com.mazasoft.ecommerce.userservice.services.handlers;

import com.mazasoft.ecommerce.userservice.enums.EventType;

import java.util.UUID;

public interface OutboxHandler<T> {
    EventType eventType();
    void handle(UUID aggregateId, T payload);
}
