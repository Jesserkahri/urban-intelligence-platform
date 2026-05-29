package com.urban.intelligence.platform.dto.predictive;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentForecastResponse {
    private Long districtId;
    private String districtName;
    private Integer historyWindowDays;
    private Integer forecastWindowDays;
    private Double baselineDailyAverage;
    private Double growthRatePercentage;
    private Integer predictedIncidentCount;
    private String trendDirection;
    private Double confidence;
    private List<ForecastPoint> forecast;
    private LocalDateTime generatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastPoint {
        private LocalDate date;
        private Double predictedCount;
        private Double lowerBound;
        private Double upperBound;
    }
}
