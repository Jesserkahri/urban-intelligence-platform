package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistrictRiskAnalysisResponse {
    private Long districtId;
    private String districtName;
    private Double riskScore;
    private String riskLevel;
    private Double incidentDensityFactor;
    private Double unresolvedRatioFactor;
    private Double averageSeverityFactor;
    private Double sustainabilityImpactFactor;
    private Integer totalIncidents;
    private Integer unresolvedIncidents;
    private Double sustainabilityScore;
    private Integer population;
}
