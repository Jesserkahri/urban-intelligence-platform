package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityScoreResponse {
    private Long id;
    private Long districtId;
    
    @JsonProperty("district_name")
    private String districtName;
    
    @JsonProperty("overall_score")
    private Double overallScore;
    
    @JsonProperty("environmental_score")
    private Double environmentalScore;
    
    @JsonProperty("mobility_score")
    private Double mobilityScore;
    
    @JsonProperty("energy_score")
    private Double energyScore;
    
    @JsonProperty("waste_score")
    private Double wasteScore;
    
    private String rating;
    private String trend;
    
    @JsonProperty("trend_percentage")
    private Double trendPercentage;
    
    @JsonProperty("calculated_at")
    private String calculatedAt;
}
