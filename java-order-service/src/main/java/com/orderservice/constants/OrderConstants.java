package com.orderservice.constants;

public final class OrderConstants {

    private OrderConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String OPERATION_CANCEL = "cancel";
    public static final String OPERATION_CONFIRM = "confirm";
    public static final String OPERATION_CREATE = "create";
}
