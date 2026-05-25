package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistrictRiskRankingResponse {
    private Long districtId;
    private String districtName;
    private Double riskScore;
    private String riskLevel;
    private Integer incidentCount;
    private Integer unresolvedCount;
    private Integer population;
}
