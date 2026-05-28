package com.urban.intelligence.platform.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityMetricCreateRequest {
    
    @NotNull(message = "District ID is required")
    private Long districtId;
    
    @NotBlank(message = "Metric type is required")
    private String metricType;
    
    @NotNull(message = "Value is required")
    @Min(value = 0, message = "Value must be non-negative")
    private Double value;
    
    @NotBlank(message = "Unit is required")
    private String unit;
    
    @NotNull(message = "Threshold is required")
    private Double threshold;
    
    private String source;
}
