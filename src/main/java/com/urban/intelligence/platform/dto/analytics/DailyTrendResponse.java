package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTrendResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalIncidents;
    private Double averageDaily;
    private Double growthPercentage;
    private String trendIndicator;
    private List<DailyData> dailyData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyData {
        private LocalDate date;
        private Integer incidentCount;
        private Integer criticalCount;
        private Integer resolvedCount;
    }
}
