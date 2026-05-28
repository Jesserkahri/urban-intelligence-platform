package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.analytics.SustainabilityAnalyticsService;
import com.urban.intelligence.platform.domain.entity.SustainabilityScore;
import com.urban.intelligence.platform.dto.SustainabilityScoreResponse;
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
        SustainabilityScore score = analyticsService.calculateDistrictSustainability(districtId);
        return mapToResponse(score);
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScoreResponse> getRanking() {
        return analyticsService.getSustainabilityRanking().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScoreResponse> getImprovingDistricts() {
        return analyticsService.getImprovingDistricts().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScoreResponse> getDecliningDistricts() {
        return analyticsService.getDecliningDistricts().stream()
                .map(this::mapToResponse)
                .toList();
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
