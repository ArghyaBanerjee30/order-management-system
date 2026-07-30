package com.orderservice.constants;

public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String ORDER_NOT_FOUND = "Order not found with id: %d";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found with id: %d";
    public static final String INVALID_ORDER_STATUS = "Cannot %s order %d: current status is %s";
    public static final String INVENTORY_SERVICE_UNAVAILABLE = "Inventory service unavailable: %s";
    public static final String CUSTOMER_SERVICE_UNAVAILABLE = "Customer service unavailable";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred: %s";
    public static final String ORDER_CREATION_FAILED = "Failed to create order: %s";
    public static final String INSUFFICIENT_INVENTORY = "Insufficient inventory for product %d";
    public static final String INVENTORY_RESERVATION_FAILED = "Inventory reservation failed: %s";
}
