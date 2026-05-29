package com.urban.intelligence.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictMetricsResponse {
    private Long id;
    private String name;

    private Double sustainabilityScore;

    private Double operationalRiskScore;

    private Double healthScore;

    private Integer recentIncidentsCount;
}
