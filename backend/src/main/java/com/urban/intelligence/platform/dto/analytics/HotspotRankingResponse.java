package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotspotRankingResponse {
    private Integer rank;
    private String districtName;
    private Double hotspotScore;
    private String criticalityLevel;
    private Integer unresolvedIncidents;
    private String averageSeverity;
}
