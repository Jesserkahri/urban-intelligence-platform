package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyTrendResponse {
    private Integer weeksAnalyzed;
    private Integer totalIncidents;
    private Double averageWeekly;
    private List<WeeklyData> weeklyData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeeklyData {
        private Integer week;
        private Integer incidentCount;
        private Map<String, Integer> categoryBreakdown;
        private Map<String, Integer> severityDistribution;
        private Double resolutionRate;
    }
}
