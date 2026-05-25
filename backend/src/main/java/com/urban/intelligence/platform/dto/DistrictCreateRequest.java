package com.urban.intelligence.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DistrictCreateRequest DTO - Input DTO for creating districts
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictCreateRequest {

    @NotBlank(message = "District name is required")
    private String name;

    @NotNull(message = "Population is required")
    @Positive(message = "Population must be positive")
    private Integer population;

    @NotNull(message = "Sustainability score is required")
    private Double sustainabilityScore;

    @NotNull(message = "Operational risk score is required")
    private Double operationalRiskScore;
}
