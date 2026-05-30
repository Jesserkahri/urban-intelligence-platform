package com.urban.intelligence.platform.dto.intelligence;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AnomalyResponse {
    private Long districtId;
    private String districtName;
    private LocalDate date;
    private double expectedValue;
    private double actualValue;
    private double rolling7DayAverage;
    private double rolling30DayAverage;
    private double standardDeviation;
    private double anomalyScore;
    private double deviationPercentage;
    private double confidence;
    private String direction;
    private String explanation;
}
