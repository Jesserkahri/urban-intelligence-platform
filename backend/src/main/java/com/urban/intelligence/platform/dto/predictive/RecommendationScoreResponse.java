package com.urban.intelligence.platform.dto.predictive;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationScoreResponse {
    private Long recommendationId;
    private Long districtId;
    private String districtName;
    private String type;
    private String priority;
    private Double predictedImpact;
    private Double interventionEffectiveness;
    private Double operationalConfidence;
    private String scoringRationale;
}
