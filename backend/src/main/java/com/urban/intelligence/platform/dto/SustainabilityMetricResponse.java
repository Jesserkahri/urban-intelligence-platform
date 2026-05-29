package com.urban.intelligence.platform.dto;

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
    
    private String metricType;
    
    private Double value;
    private String unit;
    private Double threshold;
    private String status;
    private String source;
    
    private LocalDateTime timestamp;
}
