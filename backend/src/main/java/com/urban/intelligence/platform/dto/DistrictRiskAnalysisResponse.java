package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * DistrictRiskAnalysisResponse DTO - Detailed district risk analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictRiskAnalysisResponse {

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("overall_risk_score")
    private Double overallRiskScore;

    @JsonProperty("risk_level")
    private String riskLevel; // LOW, MODERATE, HIGH, CRITICAL

    @JsonProperty("risk_components")
    private RiskComponents riskComponents;

    @JsonProperty("incident_metrics")
    private IncidentMetrics incidentMetrics;

    @JsonProperty("operational_status")
    private String operationalStatus;

    @JsonProperty("rank_among_all_districts")
    private Integer rank;

    @JsonProperty("previous_score")
    private Double previousScore;

    @JsonProperty("score_change")
    private Double scoreChange;

    @JsonProperty("trend_direction")
    private String trendDirection; // UP, DOWN, STABLE
}

/**
 * RiskComponents - Breakdown of risk calculation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class RiskComponents {

    @JsonProperty("incident_frequency_score")
    private Double incidentFrequencyScore;

    @JsonProperty("unresolved_ratio_score")
    private Double unresolvedRatioScore;

    @JsonProperty("average_severity_score")
    private Double averageSeverityScore;

    @JsonProperty("hotspot_intensity_score")
    private Double hotspotIntensityScore;

    @JsonProperty("sustainability_impact_score")
    private Double sustainabilityImpactScore;

    @JsonProperty("weights")
    private Map<String, Double> weights;
}

/**
 * IncidentMetrics - Incident-related metrics
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class IncidentMetrics {

    @JsonProperty("total_incidents_7_days")
    private Long totalIncidents7Days;

    @JsonProperty("unresolved_incidents")
    private Long unresolvedIncidents;

    @JsonProperty("critical_incidents")
    private Long criticalIncidents;

    @JsonProperty("high_incidents")
    private Long highIncidents;

    @JsonProperty("average_severity")
    private Double averageSeverity;

    @JsonProperty("resolution_rate")
    private Double resolutionRate;
}

/**
 * DistrictRiskRankingResponse - Ranking of districts by risk
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DistrictRiskRankingResponse {

    @JsonProperty("rank")
    private Integer rank;

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("total_incidents_7_days")
    private Long totalIncidents7Days;

    @JsonProperty("critical_count")
    private Long criticalCount;
}
