package com.mazasoft.ecommerce.userservice.services.database;

import com.mazasoft.ecommerce.userservice.entities.OutboxEvent;
import com.mazasoft.ecommerce.userservice.repositories.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public OutboxEvent create(OutboxEvent event) {
        return eventRepository.save(event);
    }

    @Transactional
    public OutboxEvent update(OutboxEvent event) {
        UUID id = event.getId();
        if (id == null) {
            throw new IllegalArgumentException("Event id is required for update");
        }

        OutboxEvent existing = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));

        existing.setAggregateType(event.getAggregateType());
        existing.setAggregateId(event.getAggregateId());
        existing.setEventType(event.getEventType());
        existing.setPayload(event.getPayload());
        existing.setStatus(event.getStatus());
        existing.setRetryCount(event.getRetryCount());
        existing.setLastError(event.getLastError());
        existing.setNextRetryAt(event.getNextRetryAt());
        existing.setProcessedAt(event.getProcessedAt());

        return eventRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OutboxEvent getById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));
    }
}
