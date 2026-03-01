package com.mazasoft.ecommerce.userservice.repositories;
import com.mazasoft.ecommerce.userservice.entities.OutboxEvent;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT * FROM outbox_event
        WHERE status IN ('NEW','PROCESSING')
          AND (
            status = 'NEW'
            OR (status = 'PROCESSING' AND locked_until <= now())
          )
          AND (next_retry_at IS NULL OR next_retry_at <= now())
        ORDER BY created_at
        FOR UPDATE SKIP LOCKED
        LIMIT 1
        """, nativeQuery = true)
    Optional<OutboxEvent> lockNextCandidate();

    @Modifying
    @Query(value = """
        UPDATE outbox_event
        SET status = 'PROCESSING',
            locked_until = :lockedUntil,
            locked_by = :lockedBy
        WHERE id = :id
        """, nativeQuery = true)
    int markProcessing(@Param("id") UUID id,
                       @Param("lockedUntil") Instant lockedUntil,
                       @Param("lockedBy") String lockedBy);

    @Modifying
    @Query(value = """
        UPDATE outbox_event
        SET status = 'DONE',
            processed_at = :processedAt,
            locked_until = NULL,
            locked_by = NULL,
            last_error = NULL
        WHERE id = :id
        """, nativeQuery = true)
    int markDone(@Param("id") UUID id, @Param("processedAt") Instant processedAt);

    @Modifying
    @Query(value = """
        UPDATE outbox_event
        SET status = CASE WHEN :failed THEN 'FAILED' ELSE 'NEW' END,
            retry_count = :retryCount,
            next_retry_at = :nextRetryAt,
            last_error = :lastError,
            locked_until = NULL,
            locked_by = NULL
        WHERE id = :id
        """, nativeQuery = true)
    int markRetryOrFailed(@Param("id") UUID id,
                          @Param("failed") boolean failed,
                          @Param("retryCount") int retryCount,
                          @Param("nextRetryAt") Instant nextRetryAt,
                          @Param("lastError") String lastError);
}
