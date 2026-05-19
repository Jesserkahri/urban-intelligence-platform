package com.urban.intelligence.platform.api.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ApiError - Unified error response structure
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private Integer status;
    private String message;
    private String error;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("validation_errors")
    private List<ValidationError> validationErrors;

    public void addValidationError(String field, String message) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(new ValidationError(field, message));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
