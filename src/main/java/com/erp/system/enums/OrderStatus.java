package com.erp.system.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    READY,
    DISPATCHED,
    DELIVERED,
    /** Generators physically returned — stock released immediately. Billing may still be pending. */
    COMPLETED,
    CANCELLED
}
