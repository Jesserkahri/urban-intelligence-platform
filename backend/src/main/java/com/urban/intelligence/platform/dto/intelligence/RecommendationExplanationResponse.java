package com.urban.intelligence.platform.dto.intelligence;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RecommendationExplanationResponse {
    private Long recommendationId;
    private Long districtId;
    private String districtName;
    private String type;
    private String priority;
    private String reasoning;
    private String impact;
    private double confidence;
    private Map<String, Object> evidence;
}
