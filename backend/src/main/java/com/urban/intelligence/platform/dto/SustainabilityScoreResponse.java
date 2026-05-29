package com.urban.intelligence.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityScoreResponse {
    private Long id;
    private Long districtId;
    
    private String districtName;

    private Double overallScore;

    private Double environmentalScore;

    private Double mobilityScore;

    private Double energyScore;

    private Double wasteScore;
    
    private String rating;
    private String trend;
    
    private Double trendPercentage;

    private String calculatedAt;
}
