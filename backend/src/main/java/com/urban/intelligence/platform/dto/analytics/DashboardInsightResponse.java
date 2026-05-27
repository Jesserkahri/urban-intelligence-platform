package com.urban.intelligence.platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInsightResponse {

    private SystemHealth health;
    private List<AlertSummary> alerts;
    private List<RecommendationSummary> recommendations;
    private TrendSummary trendSummary;
    private List<InsightCard> intelligenceCards;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealth {
        private Integer totalIncidents24h;
        private Integer criticalIncidents;
        private Integer highSeverityIncidents;
        private String systemStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertSummary {
        private String type;
        private String message;
        private String priority;
        private Integer count;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationSummary {
        private String title;
        private String rationale;
        private String priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendSummary {
        private String trendDirection;
        private Double growthPercentage;
        private String summary;
        private List<String> topIncidentTypes;
        private List<String> topCategories;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsightCard {
        private String title;
        private String detail;
        private String severity;
    }
}
