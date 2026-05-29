package com.urban.intelligence.platform.dto.predictive;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictRiskForecastResponse {
    private Long districtId;
    private String districtName;
    private Double currentRiskScore;
    private Double predictedRiskScore;
    private String currentRiskLevel;
    private String predictedRiskLevel;
    private Double riskDelta;
    private String driverSummary;
    private Double confidence;
    private Integer forecastWindowDays;
    private LocalDateTime generatedAt;
}
