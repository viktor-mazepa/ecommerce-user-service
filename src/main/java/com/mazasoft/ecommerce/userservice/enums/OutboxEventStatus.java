package com.mazasoft.ecommerce.userservice.enums;

public enum OutboxEventStatus {
    NEW,
    PROCESSING,
    DONE,
    FAILED
}