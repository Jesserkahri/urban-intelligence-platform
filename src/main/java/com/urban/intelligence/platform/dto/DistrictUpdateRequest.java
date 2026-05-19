package com.urban.intelligence.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictUpdateRequest {
    private String name;
    private Integer population;
    private Double sustainabilityScore;
    private Double operationalRiskScore;
}
