package com.orderservice.constants;

public final class ValidationMessages {

    private ValidationMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String CUSTOMER_ID_REQUIRED = "Customer ID is required";
    public static final String ORDER_STATUS_REQUIRED = "Order status is required";
    public static final String TOTAL_AMOUNT_REQUIRED = "Total amount is required";
    public static final String TOTAL_AMOUNT_NON_NEGATIVE = "Total amount must be non-negative";

    public static final String ORDER_REQUIRED = "Order is required";
    public static final String PRODUCT_ID_REQUIRED = "Product ID is required";
    public static final String QUANTITY_REQUIRED = "Quantity is required";
    public static final String QUANTITY_MIN = "Quantity must be at least 1";
    public static final String PRICE_REQUIRED = "Price is required";
    public static final String PRICE_NON_NEGATIVE = "Price must be non-negative";
    public static final String SUBTOTAL_REQUIRED = "Subtotal is required";
    public static final String SUBTOTAL_NON_NEGATIVE = "Subtotal must be non-negative";

    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String ORDER_ITEMS_REQUIRED = "Order must have at least one item";
}
