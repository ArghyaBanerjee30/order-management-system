package com.orderservice.entity;

/**
 * Enum representing the status of an order in the system.
 */
public enum OrderStatus {
    /**
     * Order has been created but not yet confirmed (awaiting inventory reservation)
     */
    DRAFT,

    /**
     * Order has been confirmed and inventory has been reserved
     */
    CONFIRMED,

    /**
     * Order has been cancelled and inventory has been released
     */
    CANCELLED,

    /**
     * Order processing failed (e.g., inventory reservation failed)
     */
    FAILED
}
