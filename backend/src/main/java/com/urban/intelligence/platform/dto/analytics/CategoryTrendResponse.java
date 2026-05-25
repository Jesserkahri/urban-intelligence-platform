package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTrendResponse {
    private Integer analysisWindow;
    private Integer totalIncidents;
    private Integer uniqueCategories;
    private String topCategory;
    private List<CategoryData> categoryData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryData {
        private String category;
        private Integer count;
        private Double percentage;
        private Double averageSeverity;
        private Double resolutionRate;
    }
}
