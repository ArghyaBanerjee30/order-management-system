package com.customerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;
    private Map<String, String> errors;

    public ValidationErrorResponse(int status, String message, String path, Map<String, String> errors) {
        this(LocalDateTime.now(), status, message, path, errors);
    }
}
