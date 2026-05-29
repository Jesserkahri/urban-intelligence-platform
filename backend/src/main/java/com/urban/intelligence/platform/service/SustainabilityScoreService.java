package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.analytics.SustainabilityAnalyticsService;
import com.urban.intelligence.platform.domain.entity.SustainabilityScore;
import com.urban.intelligence.platform.dto.EnvironmentalSummaryResponse;
import com.urban.intelligence.platform.dto.MobilitySummaryResponse;
import com.urban.intelligence.platform.dto.SustainabilityDashboardResponse;
import com.urban.intelligence.platform.dto.SustainabilityScoreResponse;
import com.urban.intelligence.platform.dto.SustainabilityTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SustainabilityScoreService {

    private final SustainabilityAnalyticsService analyticsService;

    @Transactional
    public SustainabilityScoreResponse calculateDistrictScore(Long districtId) {
        return mapToResponse(analyticsService.calculateDistrictSustainability(districtId));
    }

    @Transactional
    public void calculateAllScores() {
        analyticsService.calculateAllDistrictSustainability();
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScoreResponse> getRanking() {
        return analyticsService.getSustainabilityRanking().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScoreResponse> getImprovingDistricts() {
        return analyticsService.getImprovingDistricts().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScoreResponse> getDecliningDistricts() {
        return analyticsService.getDecliningDistricts().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public SustainabilityDashboardResponse getOperationsDashboard() {
        return analyticsService.getOperationsDashboard();
    }

    @Transactional(readOnly = true)
    public SustainabilityDashboardResponse getDistrictDashboard(Long districtId) {
        return analyticsService.getDistrictDashboard(districtId);
    }

    @Transactional(readOnly = true)
    public EnvironmentalSummaryResponse getEnvironmentalSummary(int days) {
        return analyticsService.getEnvironmentalSummary(days);
    }

    @Transactional(readOnly = true)
    public MobilitySummaryResponse getMobilitySummary(int days) {
        return analyticsService.getMobilitySummary(days);
    }

    @Transactional(readOnly = true)
    public SustainabilityTrendResponse getTrendEvolution(Long districtId, int days) {
        return analyticsService.getTrendEvolution(districtId, days);
    }

    private SustainabilityScoreResponse mapToResponse(SustainabilityScore score) {
        return SustainabilityScoreResponse.builder()
                .id(score.getId())
                .districtId(score.getDistrict().getId())
                .districtName(score.getDistrict().getName())
                .overallScore(score.getOverallScore())
                .environmentalScore(score.getEnvironmentalScore())
                .mobilityScore(score.getMobilityScore())
                .energyScore(score.getEnergyScore())
                .wasteScore(score.getWasteScore())
                .rating(score.getRating())
                .trend(score.getTrend())
                .trendPercentage(score.getTrendPercentage())
                .calculatedAt(score.getCalculatedAt().toString())
                .build();
    }
}
