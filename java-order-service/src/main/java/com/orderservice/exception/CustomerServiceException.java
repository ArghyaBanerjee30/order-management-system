package com.orderservice.exception;

/**
 * Exception thrown when there is an error communicating with the Customer Service.
 */
public class CustomerServiceException extends RuntimeException {

    public CustomerServiceException(String message) {
        super(message);
    }

    public CustomerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
