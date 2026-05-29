package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobilitySummaryResponse {
    private Integer analysisWindowDays;
    private Integer metricCount;
    private Double congestionEfficiency;
    private Double averageCongestion;
    private Double averageMobilityFlow;
    private Double transportationPerformance;
    private String operationalStatus;
    private LocalDateTime generatedAt;
}
