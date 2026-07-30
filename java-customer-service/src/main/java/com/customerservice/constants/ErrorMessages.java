package com.customerservice.constants;

public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String CUSTOMER_NOT_FOUND = "Customer not found with id: %d";
    public static final String CUSTOMER_DUPLICATE_EMAIL = "Customer with email %s already exists during %s";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
}
