CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS users;
SET search_path TO users;

CREATE TABLE outbox_event
(
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL, -- 'USER'
    aggregate_id   UUID         NOT NULL, -- user.id из БД
    event_type     VARCHAR(100) NOT NULL, -- USER_PROVISION_REQUESTED
    payload        JSONB        NOT NULL,
    status         VARCHAR(20)  NOT NULL, -- NEW | PROCESSING | DONE | FAILED
    retry_count    INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    next_retry_at  TIMESTAMP,
    locked_until TIMESTAMP NULL,
    locked_by VARCHAR(100) NULL,
    last_error     TEXT,
    processed_at   TIMESTAMP
);

CREATE INDEX idx_outbox_status_next_retry
    ON outbox_event (status, next_retry_at);

CREATE INDEX idx_outbox_aggregate
    ON outbox_event (aggregate_type, aggregate_id);