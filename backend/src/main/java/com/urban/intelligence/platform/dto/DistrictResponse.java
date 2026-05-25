package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictResponse {
    private Long id;
    private String name;
    private Integer population;

    @JsonProperty("sustainability_score")
    private Double sustainabilityScore;

    @JsonProperty("operational_risk_score")
    private Double operationalRiskScore;

    @JsonProperty("incident_count")
    private Integer incidentCount;

    @JsonProperty("recommendation_count")
    private Integer recommendationCount;
}
