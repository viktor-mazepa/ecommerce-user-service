package com.mazasoft.ecommerce.userservice.services.handlers;

import com.mazasoft.ecommerce.userservice.enums.EventType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OutboxHandlerRegistry {
    private final Map<EventType, OutboxHandler<?>> handlers;

    public OutboxHandlerRegistry(List<OutboxHandler<?>> list) {
        this.handlers = list.stream().collect(Collectors.toMap(OutboxHandler::eventType, h -> h));
    }

    public OutboxHandler<?> get(EventType eventType) {
        return Optional.ofNullable(handlers.get(eventType))
                .orElseThrow(() -> new IllegalStateException("No handler for " + eventType));
    }
}
