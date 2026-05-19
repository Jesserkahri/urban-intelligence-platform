package com.urban.intelligence.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * AnalyticsEventCreateRequest DTO - Input DTO for creating analytics events
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEventCreateRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Score is required")
    private Double score;

    @NotBlank(message = "Source is required")
    private String source;

    private String metadata;
}
