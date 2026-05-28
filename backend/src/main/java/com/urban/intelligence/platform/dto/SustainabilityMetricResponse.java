package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityMetricResponse {
    private Long id;
    private Long districtId;
    private String type;
    
    @JsonProperty("metric_type")
    private String metricType;
    
    private Double value;
    private String unit;
    private Double threshold;
    private String status;
    private String source;
    
    @JsonProperty("created_at")
    private LocalDateTime timestamp;
}
