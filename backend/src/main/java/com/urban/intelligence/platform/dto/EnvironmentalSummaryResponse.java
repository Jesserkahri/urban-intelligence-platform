package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentalSummaryResponse {
    private Integer analysisWindowDays;
    private Integer totalMetrics;
    private Integer criticalAlerts;
    private Double environmentalRiskScore;
    private Double averageAirQuality;
    private Double averageEmissions;
    private Double averageWasteGeneration;
    private Map<String, Long> statusDistribution;
    private LocalDateTime generatedAt;
}
