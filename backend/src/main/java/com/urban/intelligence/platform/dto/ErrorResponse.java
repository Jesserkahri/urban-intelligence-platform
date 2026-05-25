package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * ErrorResponse - Standard error response with structured details.
 *
 * Replaces raw ApiError for cleaner separation.
 * Includes optional field_errors map for validation failures.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @Builder.Default
    private boolean success = false;

    private String code;

    private String message;

    @JsonProperty("timestamp")
    @Builder.Default
    private Instant timestamp = Instant.now();

    @JsonProperty("field_errors")
    private Map<String, String> fieldErrors;
}