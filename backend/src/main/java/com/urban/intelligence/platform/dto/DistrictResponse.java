package com.urban.intelligence.platform.dto;

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

    private Double sustainabilityScore;

    private Double operationalRiskScore;

    private Integer incidentCount;

    private Integer recommendationCount;
}
