package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictMetricsResponse {
    private Long id;
    private String name;

    @JsonProperty("sustainability_score")
    private Double sustainabilityScore;

    @JsonProperty("operational_risk_score")
    private Double operationalRiskScore;

    @JsonProperty("health_score")
    private Double healthScore;

    @JsonProperty("recent_incidents_count")
    private Integer recentIncidentsCount;
}
