package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotspotRankingResponse {
    private Integer rank;
    private Long districtId;
    private String districtName;
    private Double hotspotScore;
    private Integer incidentCount;
    private Integer unresolvedIncidents;
    private Double unresolvedRatio;
    private String criticalityLevel;
    private String averageSeverity;
}
