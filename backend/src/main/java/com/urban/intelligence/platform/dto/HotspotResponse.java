package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * HotspotResponse DTO - Represents geographic incident hotspot analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotspotResponse {

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("hotspot_score")
    private Double hotspotScore;

    @JsonProperty("hotspot_level")
    private String hotspotLevel; // LOW, MODERATE, HIGH, CRITICAL

    @JsonProperty("incident_count")
    private Long incidentCount;

    @JsonProperty("unresolved_count")
    private Long unresolvedCount;

    @JsonProperty("average_severity")
    private Double averageSeverity;

    @JsonProperty("severity_breakdown")
    private Map<String, Long> severityBreakdown;

    @JsonProperty("last_updated")
    private String lastUpdated;
}

/**
 * TopHotspotResponse DTO - Represents top hotspots
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class TopHotspotResponse {

    @JsonProperty("rank")
    private Integer rank;

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("hotspot_score")
    private Double hotspotScore;

    @JsonProperty("hotspot_level")
    private String hotspotLevel;

    @JsonProperty("critical_incident_count")
    private Long criticalIncidentCount;

    @JsonProperty("high_incident_count")
    private Long highIncidentCount;
}
