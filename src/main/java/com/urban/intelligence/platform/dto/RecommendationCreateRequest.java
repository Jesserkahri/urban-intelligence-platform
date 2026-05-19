package com.urban.intelligence.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * RecommendationCreateRequest DTO - Input DTO for creating recommendations
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationCreateRequest {

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Priority is required")
    private String priority;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "District ID is required")
    private Long districtId;
}
