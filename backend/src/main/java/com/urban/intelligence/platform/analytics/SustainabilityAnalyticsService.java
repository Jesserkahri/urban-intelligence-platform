package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.SustainabilityMetric;
import com.urban.intelligence.platform.domain.entity.SustainabilityScore;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.SustainabilityMetricRepository;
import com.urban.intelligence.platform.domain.repository.SustainabilityScoreRepository;
import com.urban.intelligence.platform.dto.EnvironmentalSummaryResponse;
import com.urban.intelligence.platform.dto.MobilitySummaryResponse;
import com.urban.intelligence.platform.dto.SustainabilityDashboardResponse;
import com.urban.intelligence.platform.dto.SustainabilityMetricResponse;
import com.urban.intelligence.platform.dto.SustainabilityScoreResponse;
import com.urban.intelligence.platform.dto.SustainabilityTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SustainabilityAnalyticsService - Calculates sustainability scores and operations-center insights.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SustainabilityAnalyticsService {

    private static final int DEFAULT_DASHBOARD_WINDOW_DAYS = 30;

    private final SustainabilityMetricRepository metricRepository;
    private final SustainabilityScoreRepository scoreRepository;
    private final DistrictRepository districtRepository;

    @Transactional
    public SustainabilityScore calculateDistrictSustainability(Long districtId) {
        log.info("Calculating sustainability score for district: {}", districtId);

        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));

        SustainabilityScore previousScore = scoreRepository.findFirstByDistrictIdOrderByCalculatedAtDesc(districtId)
                .orElse(null);

        double environmentalScore = calculateEnvironmentalScore(districtId);
        double mobilityScore = calculateMobilityScore(districtId);
        double energyScore = calculateEnergyScore(districtId);
        double wasteScore = calculateWasteScore(districtId);
        double overallScore = (environmentalScore * 0.35)
                + (mobilityScore * 0.25)
                + (energyScore * 0.25)
                + (wasteScore * 0.15);

        String trend = "STABLE";
        double trendPercentage = 0.0;
        if (previousScore != null && previousScore.getOverallScore() > 0) {
            trendPercentage = ((overallScore - previousScore.getOverallScore()) / previousScore.getOverallScore()) * 100;
            if (trendPercentage > 2) trend = "IMPROVING";
            else if (trendPercentage < -2) trend = "DECLINING";
        }

        SustainabilityScore score = SustainabilityScore.builder()
                .district(district)
                .overallScore(round(Math.min(overallScore, 100.0)))
                .environmentalScore(round(environmentalScore))
                .mobilityScore(round(mobilityScore))
                .energyScore(round(energyScore))
                .wasteScore(round(wasteScore))
                .trend(trend)
                .trendPercentage(round(trendPercentage))
                .calculatedAt(LocalDateTime.now())
                .previousCalculation(previousScore == null ? LocalDateTime.now() : previousScore.getCalculatedAt())
                .build();

        SustainabilityScore saved = scoreRepository.save(score);
        district.setSustainabilityScore(saved.getOverallScore());
        districtRepository.save(district);
        return saved;
    }

    private double calculateEnvironmentalScore(Long districtId) {
        List<SustainabilityMetric> metrics = new ArrayList<>();
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "AIR_QUALITY"));
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "EMISSIONS"));
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "GREEN_SPACE"));
        if (metrics.isEmpty()) return 75.0;
        return metrics.stream().mapToDouble(this::calculateMetricScore).average().orElse(75.0);
    }

    private double calculateMobilityScore(Long districtId) {
        List<SustainabilityMetric> metrics = new ArrayList<>();
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "CONGESTION"));
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "MOBILITY_FLOW"));
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "TRANSIT_EFFICIENCY"));
        if (metrics.isEmpty()) return 70.0;
        return metrics.stream().mapToDouble(this::calculateMetricScore).average().orElse(70.0);
    }

    private double calculateEnergyScore(Long districtId) {
        List<SustainabilityMetric> metrics = new ArrayList<>();
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "ENERGY_CONSUMPTION"));
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "RENEWABLE_ENERGY"));
        if (metrics.isEmpty()) return 65.0;
        return metrics.stream().mapToDouble(this::calculateMetricScore).average().orElse(65.0);
    }

    private double calculateWasteScore(Long districtId) {
        List<SustainabilityMetric> metrics = new ArrayList<>();
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "WASTE_GENERATION"));
        metrics.addAll(metricRepository.findByDistrict_IdAndMetricType(districtId, "RECYCLING_RATE"));
        if (metrics.isEmpty()) return 60.0;
        return metrics.stream().mapToDouble(this::calculateMetricScore).average().orElse(60.0);
    }

    private double calculateMetricScore(SustainabilityMetric metric) {
        return switch (metric.getStatus()) {
            case "GOOD" -> 95.0;
            case "MODERATE" -> 70.0;
            case "POOR" -> 45.0;
            case "CRITICAL" -> 10.0;
            default -> 50.0;
        };
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScore> getSustainabilityRanking() {
        return getLatestScores().stream()
                .sorted(Comparator.comparingDouble(SustainabilityScore::getOverallScore).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScore> getImprovingDistricts() {
        return getSustainabilityRanking().stream()
                .filter(score -> "IMPROVING".equals(score.getTrend()))
                .sorted(Comparator.comparingDouble(SustainabilityScore::getTrendPercentage).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SustainabilityScore> getDecliningDistricts() {
        return getSustainabilityRanking().stream()
                .filter(score -> "DECLINING".equals(score.getTrend()))
                .sorted(Comparator.comparingDouble(SustainabilityScore::getTrendPercentage))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SustainabilityMetric> getEnvironmentalAlerts() {
        return metricRepository.findCriticalMetrics();
    }

    @Transactional
    public void calculateAllDistrictSustainability() {
        List<District> districts = districtRepository.findAll();
        districts.forEach(district -> {
            try {
                calculateDistrictSustainability(district.getId());
            } catch (Exception e) {
                log.warn("Failed to calculate sustainability for district {}: {}", district.getId(), e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public SustainabilityDashboardResponse getOperationsDashboard() {
        List<SustainabilityScore> ranking = getSustainabilityRanking();
        List<SustainabilityMetric> recentMetrics = metricRepository.findByTimestampAfter(
                LocalDateTime.now().minusDays(DEFAULT_DASHBOARD_WINDOW_DAYS));
        List<SustainabilityMetric> alerts = metricRepository.findCriticalMetrics();

        double averageOverall = ranking.stream().mapToDouble(SustainabilityScore::getOverallScore).average().orElse(0.0);
        double averageEnvironmental = ranking.stream().mapToDouble(SustainabilityScore::getEnvironmentalScore).average().orElse(0.0);
        double averageMobility = ranking.stream().mapToDouble(SustainabilityScore::getMobilityScore).average().orElse(0.0);
        double environmentalRisk = recentMetrics.isEmpty() ? 0.0 : (alerts.size() / (double) recentMetrics.size()) * 100;

        return SustainabilityDashboardResponse.builder()
                .kpis(List.of(
                        kpi("overall_sustainability", "Overall Sustainability", averageOverall, "score", statusForScore(averageOverall), 0.0),
                        kpi("environmental_quality", "Environmental Quality", averageEnvironmental, "score", statusForScore(averageEnvironmental), 0.0),
                        kpi("mobility_efficiency", "Mobility Efficiency", averageMobility, "score", statusForScore(averageMobility), 0.0),
                        kpi("environmental_alerts", "Environmental Alerts", (double) alerts.size(), "alerts", alerts.isEmpty() ? "GOOD" : "CRITICAL", 0.0),
                        kpi("environmental_risk", "Environmental Risk", environmentalRisk, "%", riskStatus(environmentalRisk), 0.0)
                ))
                .ranking(ranking.stream().limit(10).map(this::mapScore).collect(Collectors.toList()))
                .districtComparisons(ranking.stream().map(this::mapComparison).collect(Collectors.toList()))
                .trendCharts(buildMetricTrends(recentMetrics))
                .environmentalAlerts(alerts.stream().limit(20).map(this::mapMetric).collect(Collectors.toList()))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public SustainabilityDashboardResponse getDistrictDashboard(Long districtId) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));
        SustainabilityScore score = scoreRepository.findFirstByDistrictIdOrderByCalculatedAtDesc(districtId)
                .orElseGet(() -> SustainabilityScore.builder()
                        .district(district)
                        .overallScore(district.getSustainabilityScore())
                        .environmentalScore(district.getSustainabilityScore())
                        .mobilityScore(70.0)
                        .energyScore(65.0)
                        .wasteScore(60.0)
                        .rating("C")
                        .trend("STABLE")
                        .trendPercentage(0.0)
                        .calculatedAt(LocalDateTime.now())
                        .previousCalculation(LocalDateTime.now())
                        .build());
        List<SustainabilityMetric> metrics = metricRepository.findRecentMetrics(districtId, LocalDateTime.now().minusDays(DEFAULT_DASHBOARD_WINDOW_DAYS));
        List<SustainabilityMetric> alerts = metrics.stream().filter(metric -> "CRITICAL".equals(metric.getStatus())).collect(Collectors.toList());

        return SustainabilityDashboardResponse.builder()
                .kpis(List.of(
                        kpi("district_sustainability", "District Sustainability", score.getOverallScore(), "score", statusForScore(score.getOverallScore()), score.getTrendPercentage()),
                        kpi("environmental_score", "Environmental Score", score.getEnvironmentalScore(), "score", statusForScore(score.getEnvironmentalScore()), 0.0),
                        kpi("mobility_score", "Mobility Score", score.getMobilityScore(), "score", statusForScore(score.getMobilityScore()), 0.0),
                        kpi("active_alerts", "Active Alerts", (double) alerts.size(), "alerts", alerts.isEmpty() ? "GOOD" : "CRITICAL", 0.0)
                ))
                .ranking(List.of(mapScore(score)))
                .districtComparisons(List.of(mapComparison(score)))
                .trendCharts(buildMetricTrends(metrics))
                .environmentalAlerts(alerts.stream().map(this::mapMetric).collect(Collectors.toList()))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public EnvironmentalSummaryResponse getEnvironmentalSummary(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SustainabilityMetric> metrics = metricRepository.findByTimestampAfter(since).stream()
                .filter(metric -> List.of("AIR_QUALITY", "EMISSIONS", "WASTE_GENERATION", "GREEN_SPACE").contains(metric.getMetricType()))
                .collect(Collectors.toList());
        long critical = metrics.stream().filter(metric -> "CRITICAL".equals(metric.getStatus())).count();
        Map<String, Long> statusDistribution = metrics.stream()
                .collect(Collectors.groupingBy(SustainabilityMetric::getStatus, LinkedHashMap::new, Collectors.counting()));

        return EnvironmentalSummaryResponse.builder()
                .analysisWindowDays(days)
                .totalMetrics(metrics.size())
                .criticalAlerts((int) critical)
                .environmentalRiskScore(round(metrics.isEmpty() ? 0.0 : (critical / (double) metrics.size()) * 100))
                .averageAirQuality(avgMetric(metrics, "AIR_QUALITY"))
                .averageEmissions(avgMetric(metrics, "EMISSIONS"))
                .averageWasteGeneration(avgMetric(metrics, "WASTE_GENERATION"))
                .statusDistribution(statusDistribution)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public MobilitySummaryResponse getMobilitySummary(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<SustainabilityMetric> metrics = metricRepository.findByTimestampAfter(since).stream()
                .filter(metric -> List.of("CONGESTION", "MOBILITY_FLOW", "TRANSIT_EFFICIENCY").contains(metric.getMetricType()))
                .collect(Collectors.toList());
        double avgCongestion = avgMetric(metrics, "CONGESTION");
        double avgFlow = avgMetric(metrics, "MOBILITY_FLOW");
        double transit = avgMetric(metrics, "TRANSIT_EFFICIENCY");
        double congestionEfficiency = avgCongestion == 0.0 ? 100.0 : Math.max(0.0, 100.0 - avgCongestion);
        double performance = round((congestionEfficiency * 0.4) + (avgFlow * 0.3) + (transit * 0.3));

        return MobilitySummaryResponse.builder()
                .analysisWindowDays(days)
                .metricCount(metrics.size())
                .congestionEfficiency(round(congestionEfficiency))
                .averageCongestion(round(avgCongestion))
                .averageMobilityFlow(round(avgFlow))
                .transportationPerformance(performance)
                .operationalStatus(statusForScore(performance))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public SustainabilityTrendResponse getTrendEvolution(Long districtId, int days) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));
        List<SustainabilityScore> points = scoreRepository
                .findByDistrict_IdAndCalculatedAtAfterOrderByCalculatedAtAsc(districtId, LocalDateTime.now().minusDays(days));
        double change = points.size() < 2 ? 0.0
                : ((points.get(points.size() - 1).getOverallScore() - points.get(0).getOverallScore()) / points.get(0).getOverallScore()) * 100;

        return SustainabilityTrendResponse.builder()
                .districtId(districtId)
                .districtName(district.getName())
                .analysisWindowDays(days)
                .trendDirection(change > 2 ? "IMPROVING" : change < -2 ? "DECLINING" : "STABLE")
                .changePercentage(round(change))
                .points(points.stream().map(point -> SustainabilityTrendResponse.Point.builder()
                        .calculatedAt(point.getCalculatedAt())
                        .overallScore(point.getOverallScore())
                        .environmentalScore(point.getEnvironmentalScore())
                        .mobilityScore(point.getMobilityScore())
                        .energyScore(point.getEnergyScore())
                        .wasteScore(point.getWasteScore())
                        .rating(point.getRating())
                        .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private List<SustainabilityScore> getLatestScores() {
        return scoreRepository.findAll(Sort.by(Sort.Direction.DESC, "calculatedAt")).stream()
                .collect(Collectors.toMap(score -> score.getDistrict().getId(), Function.identity(), (first, ignored) -> first, LinkedHashMap::new))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    private List<SustainabilityDashboardResponse.TrendPoint> buildMetricTrends(List<SustainabilityMetric> metrics) {
        return metrics.stream()
                .filter(metric -> metric.getTimestamp() != null)
                .collect(Collectors.groupingBy(metric -> metric.getTimestamp().toLocalDate() + "|" + metric.getMetricType()))
                .entrySet()
                .stream()
                .map(entry -> {
                    String[] key = entry.getKey().split("\\|");
                    List<SustainabilityMetric> values = entry.getValue();
                    return SustainabilityDashboardResponse.TrendPoint.builder()
                            .date(LocalDate.parse(key[0]))
                            .metricType(key[1])
                            .averageValue(round(values.stream().mapToDouble(SustainabilityMetric::getValue).average().orElse(0.0)))
                            .unit(values.isEmpty() ? "" : values.get(0).getUnit())
                            .build();
                })
                .sorted(Comparator.comparing(SustainabilityDashboardResponse.TrendPoint::getDate))
                .collect(Collectors.toList());
    }

    private SustainabilityDashboardResponse.KpiCard kpi(String key, String label, Double value, String unit, String status, Double change) {
        return SustainabilityDashboardResponse.KpiCard.builder()
                .key(key)
                .label(label)
                .value(round(value))
                .unit(unit)
                .status(status)
                .changePercentage(round(change))
                .build();
    }

    private SustainabilityDashboardResponse.DistrictComparison mapComparison(SustainabilityScore score) {
        return SustainabilityDashboardResponse.DistrictComparison.builder()
                .districtId(score.getDistrict().getId())
                .districtName(score.getDistrict().getName())
                .sustainabilityScore(score.getOverallScore())
                .environmentalScore(score.getEnvironmentalScore())
                .mobilityScore(score.getMobilityScore())
                .rating(score.getRating())
                .trend(score.getTrend())
                .build();
    }

    private SustainabilityScoreResponse mapScore(SustainabilityScore score) {
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

    private SustainabilityMetricResponse mapMetric(SustainabilityMetric metric) {
        return SustainabilityMetricResponse.builder()
                .id(metric.getId())
                .districtId(metric.getDistrict().getId())
                .metricType(metric.getMetricType())
                .value(metric.getValue())
                .unit(metric.getUnit())
                .threshold(metric.getThreshold())
                .status(metric.getStatus())
                .source(metric.getSource())
                .timestamp(metric.getTimestamp())
                .build();
    }

    private double avgMetric(List<SustainabilityMetric> metrics, String metricType) {
        return round(metrics.stream()
                .filter(metric -> metricType.equals(metric.getMetricType()))
                .mapToDouble(SustainabilityMetric::getValue)
                .average()
                .orElse(0.0));
    }

    private String statusForScore(double score) {
        if (score >= 80) return "GOOD";
        if (score >= 65) return "MODERATE";
        if (score >= 50) return "POOR";
        return "CRITICAL";
    }

    private String riskStatus(double risk) {
        if (risk >= 30) return "CRITICAL";
        if (risk >= 15) return "POOR";
        if (risk >= 5) return "MODERATE";
        return "GOOD";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
