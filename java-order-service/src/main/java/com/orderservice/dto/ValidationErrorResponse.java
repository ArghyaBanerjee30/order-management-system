package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation error response DTO for field validation errors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    /**
     * Timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * General error message.
     */
    private String message;

    /**
     * API path where error occurred.
     */
    private String path;

    /**
     * List of field-specific validation errors.
     */
    private List<FieldError> errors;

    /**
     * Constructor with current timestamp and empty error list.
     *
     * @param status HTTP status code
     * @param message error message
     * @param path API path
     */
    public ValidationErrorResponse(int status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = new ArrayList<>();
    }

    /**
     * Add a field error to the list.
     *
     * @param field field name
     * @param error error message
     */
    public void addError(String field, String error) {
        this.errors.add(new FieldError(field, error));
    }

    /**
     * Field error inner class.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        /**
         * Field name that failed validation.
         */
        private String field;

        /**
         * Validation error message.
         */
        private String message;
    }
}
