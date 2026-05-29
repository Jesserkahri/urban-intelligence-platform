package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityDashboardResponse {
    private List<KpiCard> kpis;
    private List<SustainabilityScoreResponse> ranking;
    private List<DistrictComparison> districtComparisons;
    private List<TrendPoint> trendCharts;
    private List<SustainabilityMetricResponse> environmentalAlerts;
    private LocalDateTime generatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KpiCard {
        private String key;
        private String label;
        private Double value;
        private String unit;
        private String status;
        private Double changePercentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistrictComparison {
        private Long districtId;
        private String districtName;
        private Double sustainabilityScore;
        private Double environmentalScore;
        private Double mobilityScore;
        private String rating;
        private String trend;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendPoint {
        private LocalDate date;
        private String metricType;
        private Double averageValue;
        private String unit;
    }
}
