package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityTrendResponse {
    private Long districtId;
    private String districtName;
    private Integer analysisWindowDays;
    private String trendDirection;
    private Double changePercentage;
    private List<Point> points;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Point {
        private LocalDateTime calculatedAt;
        private Double overallScore;
        private Double environmentalScore;
        private Double mobilityScore;
        private Double energyScore;
        private Double wasteScore;
        private String rating;
    }
}
