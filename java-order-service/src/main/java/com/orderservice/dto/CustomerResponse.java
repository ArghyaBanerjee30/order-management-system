package com.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for customer response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer information response")
public class CustomerResponse {

    @Schema(description = "Customer's unique identifier", example = "1")
    private Long id;

    @Schema(description = "Customer's first name", example = "John")
    private String firstName;

    @Schema(description = "Customer's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Customer's phone number", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Timestamp when customer was created", example = "2026-07-21T10:30:00")
    private LocalDateTime createdAt;
}
