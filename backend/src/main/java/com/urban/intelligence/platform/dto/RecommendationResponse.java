package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private Long id;
    private String type;
    private String priority;
    private String message;

    @JsonProperty("predicted_impact")
    private Double predictedImpact;

    @JsonProperty("intervention_effectiveness")
    private Double interventionEffectiveness;

    @JsonProperty("operational_confidence")
    private Double operationalConfidence;

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
