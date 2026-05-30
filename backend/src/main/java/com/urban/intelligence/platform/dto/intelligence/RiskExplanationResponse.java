package com.urban.intelligence.platform.dto.intelligence;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RiskExplanationResponse {
    private Long districtId;
    private String districtName;
    private double riskScore;
    private String riskLevel;
    private String trendDirection;
    private double confidence;
    private List<RiskFactor> contributingFactors;
    private String explanation;

    @Data
    @Builder
    public static class RiskFactor {
        private String name;
        private double value;
        private double weight;
        private double contribution;
        private String explanation;
    }
}
