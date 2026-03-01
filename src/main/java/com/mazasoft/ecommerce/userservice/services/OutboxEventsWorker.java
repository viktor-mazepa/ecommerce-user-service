package com.mazasoft.ecommerce.userservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mazasoft.ecommerce.userservice.dto.event.UserProvisionRequestedPayload;
import com.mazasoft.ecommerce.userservice.entities.OutboxEvent;
import com.mazasoft.ecommerce.userservice.enums.OutboxEventStatus;
import com.mazasoft.ecommerce.userservice.repositories.EventRepository;
import com.mazasoft.ecommerce.userservice.services.handlers.OutboxHandler;
import com.mazasoft.ecommerce.userservice.services.handlers.OutboxHandlerRegistry;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OutboxEventsWorker {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventsWorker.class);
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final OutboxHandlerRegistry registry;
    private final OutboxStateService outboxStateService;

    private final String workerId = "user-service-1";
    private static final int MAX_RETRIES = 5;

    public OutboxEventsWorker(EventRepository eventRepository, ObjectMapper objectMapper, OutboxHandlerRegistry outboxHandlerRegistry, OutboxStateService outboxStateService) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.registry = outboxHandlerRegistry;
        this.outboxStateService = outboxStateService;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void process() {
        Optional<OutboxEvent> optional = lockNextEvent();
        if (optional.isEmpty()) return;

        OutboxEvent event = optional.get();

        Instant lockedUntil = Instant.now().plusSeconds(60);
        outboxStateService.markProcessing(event.getId(), lockedUntil, workerId);
        try {
            handleEvent(event);
            outboxStateService.markDone(event.getId());
        } catch (Exception ex) {
            log.error("Exception in OutboxEventsWorker: ", ex);
            int retry = event.getRetryCount() + 1;

            if (retry > MAX_RETRIES || isNonRetriable(ex)) {
                outboxStateService.markFailed(event.getId(), retry, shorten(ex));
            } else {
                Instant next = computeNextRetryAt(retry);
                outboxStateService.markRetry(event.getId(), retry, next, shorten(ex));
            }
        }
    }

    private void handleEvent(OutboxEvent event) throws JsonProcessingException {
        OutboxHandler handler = registry.get(event.getEventType());
        UserProvisionRequestedPayload payload = objectMapper.treeToValue(event.getPayload(), UserProvisionRequestedPayload.class);
        handler.handle(event.getAggregateId(), payload);
    }

    @Transactional
    protected Optional<OutboxEvent> lockNextEvent() {
        return eventRepository.lockNextCandidate();
    }

    private boolean isNonRetriable(Exception ex) {
        // пример: валидационные ошибки, неизвестная роль, и т.п.
        return ex instanceof IllegalArgumentException;
    }

    private Instant computeNextRetryAt(int retry) {
        // простой backoff: 10s, 30s, 2m, 10m, 1h
        long seconds = switch (retry) {
            case 1 -> 10;
            case 2 -> 30;
            case 3 -> 120;
            case 4 -> 600;
            default -> 3600;
        };
        return Instant.now().plusSeconds(seconds);
    }

    private String shorten(Exception ex) {
        String msg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        if (msg.length() > 2000) msg = msg.substring(0, 2000);
        return msg;
    }
}
