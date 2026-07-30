package com.customerservice.exception;

public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String email, String operation) {
        super("Customer with email " + email + " already exists during " + operation);
    }
}
