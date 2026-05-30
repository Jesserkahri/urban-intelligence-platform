package com.urban.intelligence.platform.dto.intelligence;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ForecastResponse {
    private Long districtId;
    private String districtName;
    private int forecastWindowDays;
    private double baselineDailyAverage;
    private double weightedMovingAverage;
    private double trendAcceleration;
    private double confidence;
    private int predictedIncidents;
    private String explanation;
    private List<ForecastPoint> forecast;

    @Data
    @Builder
    public static class ForecastPoint {
        private LocalDate date;
        private double predictedIncidents;
        private double lowerBound;
        private double upperBound;
        private double weekdaySeasonalityFactor;
        private double confidence;
    }
}
