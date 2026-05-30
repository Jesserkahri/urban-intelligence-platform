package com.urban.intelligence.platform.dto.intelligence;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpatialRiskResponse {
    private Long districtId;
    private String districtName;
    private double localIncidentDensity;
    private double neighboringDistrictDensity;
    private double hotspotSpreadFactor;
    private double districtInfluenceScore;
    private double neighboringRiskPropagationScore;
    private double spatialRisk;
    private String influenceRadius;
    private double neighboringImpact;
    private List<String> neighboringDistricts;
    private String explanation;
}
