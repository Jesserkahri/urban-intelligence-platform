package com.urban.intelligence.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * IncidentCreateRequest DTO - Input DTO for creating incidents
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentCreateRequest {

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private String severity;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "District ID is required")
    private Long districtId;
}
