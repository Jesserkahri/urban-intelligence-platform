package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotspotResponse {
    private Long districtId;
    private String districtName;
    private Double hotspotScore;
    private Integer incidentCount;
    private Integer unresolvedIncidentCount;
    private Double unresolvedRatio;
    private String averageSeverity;
    private String riskIntensity;
}
