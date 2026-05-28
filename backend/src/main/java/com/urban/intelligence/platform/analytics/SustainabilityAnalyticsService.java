package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.SustainabilityMetric;
import com.urban.intelligence.platform.domain.entity.SustainabilityScore;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.SustainabilityMetricRepository;
import com.urban.intelligence.platform.domain.repository.SustainabilityScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SustainabilityAnalyticsService - Calculates sustainability scores and environmental insights
 *
 * Provides comprehensive sustainability intelligence:
 * - Environmental metrics aggregation
 * - Sustainability scoring (0-100)
 * - Trend analysis
 * - Comparative ranking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SustainabilityAnalyticsService {

    private final SustainabilityMetricRepository metricRepository;
    private final SustainabilityScoreRepository scoreRepository;
    private final DistrictRepository districtRepository;

    /**
     * Calculate sustainability score for a district
     * Aggregates all environmental metrics into single 0-100 score
     */
    @Transactional
    public SustainabilityScore calculateDistrictSustainability(Long districtId) {
        log.info("Calculating sustainability score for district: {}", districtId);

        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));

        // Get previous score for trend analysis
        SustainabilityScore previousScore = scoreRepository.findByDistrictIdOrderByCalculatedAtDesc(districtId)
                .orElse(null);

        // Calculate component scores
        Double environmentalScore = calculateEnvironmentalScore(districtId);
        Double mobilityScore = calculateMobilityScore(districtId);
        Double energyScore = calculateEnergyScore(districtId);
        Double wasteScore = calculateWasteScore(districtId);

        // Overall score (weighted average)
        Double overallScore = (environmentalScore * 0.35 +
                mobilityScore * 0.25 +
                energyScore * 0.25 +
                wasteScore * 0.15);

        // Calculate trend
        String trend = "STABLE";
        Double trendPercentage = 0.0;
        if (previousScore != null) {
            trendPercentage = ((overallScore - previousScore.getOverallScore()) / previousScore.getOverallScore()) * 100;
            if (trendPercentage > 2) trend = "IMPROVING";
            else if (trendPercentage < -2) trend = "DECLINING";
        }

        SustainabilityScore score = SustainabilityScore.builder()
                .district(district)
                .overallScore(Math.min(overallScore, 100.0))
                .environmentalScore(environmentalScore)
                .mobilityScore(mobilityScore)
                .energyScore(energyScore)
                .wasteScore(wasteScore)
                .trend(trend)
                .trendPercentage(trendPercentage)
                .calculatedAt(LocalDateTime.now())
                .build();

        SustainabilityScore saved = scoreRepository.save(score);
        log.info("Sustainability score calculated for district {}: {}", districtId, saved.getOverallScore());
        return saved;
    }

    /**
     * Calculate environmental score from air quality, emissions metrics
     */
    private Double calculateEnvironmentalScore(Long districtId) {
        List<SustainabilityMetric> airQualityMetrics = metricRepository
                .findByDistrict_IdAndMetricType(districtId, "AIR_QUALITY");

        if (airQualityMetrics.isEmpty()) return 75.0;

        double avgScore = airQualityMetrics.stream()
                .mapToDouble(m -> calculateMetricScore(m))
                .average()
                .orElse(75.0);

        return Math.min(avgScore, 100.0);
    }

    /**
     * Calculate mobility efficiency score
     */
    private Double calculateMobilityScore(Long districtId) {
        List<SustainabilityMetric> congestionMetrics = metricRepository
                .findByDistrict_IdAndMetricType(districtId, "CONGESTION");

        if (congestionMetrics.isEmpty()) return 70.0;

        // Lower congestion = higher score
        double avgCongestion = congestionMetrics.stream()
                .mapToDouble(SustainabilityMetric::getValue)
                .average()
                .orElse(50.0);

        return Math.max(100.0 - (avgCongestion * 0.8), 20.0);
    }

    /**
     * Calculate energy sustainability score
     */
    private Double calculateEnergyScore(Long districtId) {
        List<SustainabilityMetric> energyMetrics = metricRepository
                .findByDistrict_IdAndMetricType(districtId, "ENERGY_CONSUMPTION");

        if (energyMetrics.isEmpty()) return 65.0;

        double avgScore = energyMetrics.stream()
                .mapToDouble(m -> calculateMetricScore(m))
                .average()
                .orElse(65.0);

        return Math.min(avgScore, 100.0);
    }

    /**
     * Calculate waste management score
     */
    private Double calculateWasteScore(Long districtId) {
        List<SustainabilityMetric> wasteMetrics = metricRepository
                .findByDistrict_IdAndMetricType(districtId, "WASTE_GENERATION");

        if (wasteMetrics.isEmpty()) return 60.0;

        double avgScore = wasteMetrics.stream()
                .mapToDouble(m -> calculateMetricScore(m))
                .average()
                .orElse(60.0);

        return Math.min(avgScore, 100.0);
    }

    /**
     * Convert individual metric to 0-100 score based on status
     */
    private Double calculateMetricScore(SustainabilityMetric metric) {
        return switch (metric.getStatus()) {
            case "GOOD" -> 95.0;
            case "MODERATE" -> 70.0;
            case "POOR" -> 45.0;
            case "CRITICAL" -> 10.0;
            default -> 50.0;
        };
    }

    /**
     * Get sustainability ranking of all districts
     */
    @Transactional(readOnly = true)
    public List<SustainabilityScore> getSustainabilityRanking() {
        log.debug("Fetching sustainability ranking");
        return scoreRepository.findAllOrderByScoreDesc();
    }

    /**
     * Get districts showing improvement
     */
    @Transactional(readOnly = true)
    public List<SustainabilityScore> getImprovingDistricts() {
        return scoreRepository.findImprovingDistricts();
    }

    /**
     * Get districts with declining sustainability
     */
    @Transactional(readOnly = true)
    public List<SustainabilityScore> getDecliningDistricts() {
        return scoreRepository.findDecliningDistricts();
    }

    /**
     * Get environmental alerts for critical metrics
     */
    @Transactional(readOnly = true)
    public List<SustainabilityMetric> getEnvironmentalAlerts() {
        return metricRepository.findCriticalMetrics();
    }

    /**
     * Calculate all district sustainability scores
     */
    @Transactional
    public void calculateAllDistrictSustainability() {
        log.info("Calculating sustainability scores for all districts");
        List<District> districts = districtRepository.findAll();
        districts.forEach(district -> {
            try {
                calculateDistrictSustainability(district.getId());
            } catch (Exception e) {
                log.warn("Failed to calculate sustainability for district {}: {}", district.getId(), e.getMessage());
            }
        });
        log.info("Completed sustainability calculation for {} districts", districts.size());
    }
}
