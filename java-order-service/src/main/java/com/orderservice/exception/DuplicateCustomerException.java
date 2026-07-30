package com.orderservice.exception;

/**
 * Exception thrown when attempting to create a customer with a duplicate email.
 */
public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String message) {
        super(message);
    }

    public DuplicateCustomerException(String email, String action) {
        super("Customer with email " + email + " already exists. Cannot " + action + ".");
    }
}
