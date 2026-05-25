package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;
import java.util.List;

/**
 * TrendAnalysisResponse DTO - Daily incident trend analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendAnalysisResponse {

    @JsonProperty("analysis_date")
    private String analysisDate;

    @JsonProperty("trend_type")
    private String trendType; // DAILY, WEEKLY, CATEGORY

    @JsonProperty("data_points")
    private List<TrendDataPoint> dataPoints;

    @JsonProperty("summary")
    private TrendSummary summary;
}

/**
 * TrendDataPoint - Individual data point in trend
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class TrendDataPoint {

    @JsonProperty("label")
    private String label;

    @JsonProperty("count")
    private Long count;

    @JsonProperty("severity_weighted_score")
    private Double severityWeightedScore;

    @JsonProperty("average_severity")
    private Double averageSeverity;

    @JsonProperty("percentage_change")
    private Double percentageChange;
}

/**
 * TrendSummary - Aggregated trend insights
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class TrendSummary {

    @JsonProperty("total_incidents")
    private Long totalIncidents;

    @JsonProperty("average_daily_incidents")
    private Double averageDailyIncidents;

    @JsonProperty("trend_direction")
    private String trendDirection; // UP, DOWN, STABLE

    @JsonProperty("growth_percentage")
    private Double growthPercentage;

    @JsonProperty("most_common_type")
    private String mostCommonType;

    @JsonProperty("peak_day")
    private String peakDay;

    @JsonProperty("operational_pressure")
    private String operationalPressure; // LOW, MODERATE, HIGH, CRITICAL
}

/**
 * CategoryDistributionResponse - Category-based incident distribution
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CategoryDistributionResponse {

    @JsonProperty("period")
    private String period;

    @JsonProperty("categories")
    private Map<String, CategoryMetrics> categories;

    @JsonProperty("total_incidents")
    private Long totalIncidents;
}

/**
 * CategoryMetrics - Metrics for a specific category
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CategoryMetrics {

    @JsonProperty("count")
    private Long count;

    @JsonProperty("percentage")
    private Double percentage;

    @JsonProperty("average_severity")
    private Double averageSeverity;

    @JsonProperty("severity_weighted_score")
    private Double severityWeightedScore;
}
