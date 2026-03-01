package com.mazasoft.ecommerce.userservice.services;

import com.mazasoft.ecommerce.userservice.repositories.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxStateService {

    private final EventRepository eventRepository;

    public OutboxStateService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(UUID eventId, Instant lockedUntil, String lockedBy) {
        int updated = eventRepository.markProcessing(eventId, lockedUntil, lockedBy);
        if (updated != 1) {
            throw new IllegalStateException("Failed to mark PROCESSING for outbox event: " + eventId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDone(UUID eventId) {
        int updated = eventRepository.markDone(eventId, Instant.now());
        if (updated != 1) {
            throw new IllegalStateException("Failed to mark DONE for outbox event: " + eventId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(UUID eventId, int retryCount, Instant nextRetryAt, String lastError) {
        int updated = eventRepository.markRetryOrFailed(eventId, false, retryCount, nextRetryAt, lastError);
        if (updated != 1) {
            throw new IllegalStateException("Failed to mark RETRY for outbox event: " + eventId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID eventId, int retryCount, String lastError) {
        int updated = eventRepository.markRetryOrFailed(eventId, true, retryCount, null, lastError);
        if (updated != 1) {
            throw new IllegalStateException("Failed to mark FAILED for outbox event: " + eventId);
        }
    }
}