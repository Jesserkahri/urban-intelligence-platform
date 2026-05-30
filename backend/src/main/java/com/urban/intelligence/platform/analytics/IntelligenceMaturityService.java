package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Recommendation;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.domain.repository.RecommendationRepository;
import com.urban.intelligence.platform.dto.intelligence.AnomalyResponse;
import com.urban.intelligence.platform.dto.intelligence.ForecastResponse;
import com.urban.intelligence.platform.dto.intelligence.RecommendationExplanationResponse;
import com.urban.intelligence.platform.dto.intelligence.RiskExplanationResponse;
import com.urban.intelligence.platform.dto.intelligence.SpatialRiskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntelligenceMaturityService {

    private static final int HISTORY_DAYS = 90;

    private final DistrictRepository districtRepository;
    private final IncidentRepository incidentRepository;
    private final RecommendationRepository recommendationRepository;
    private final DistrictRiskScoringService riskScoringService;

    public List<AnomalyResponse> detectIncidentAnomalies() {
        Map<Long, Map<LocalDate, Long>> seriesByDistrict = loadDailySeriesByDistrict(HISTORY_DAYS);
        Map<Long, District> districts = districtRepository.findAll().stream()
                .collect(Collectors.toMap(District::getId, district -> district));
        List<AnomalyResponse> anomalies = new ArrayList<>();

        for (Map.Entry<Long, Map<LocalDate, Long>> entry : seriesByDistrict.entrySet()) {
            District district = districts.get(entry.getKey());
            if (district == null) continue;
            List<Long> series = completeSeries(entry.getValue(), HISTORY_DAYS);
            if (series.size() < 31) continue;

            double actual = series.get(series.size() - 1);
            List<Long> baselineWindow = series.subList(Math.max(0, series.size() - 31), series.size() - 1);
            double mean30 = average(baselineWindow);
            double stdDev = stdDev(baselineWindow, mean30);
            if (stdDev == 0.0 && actual == mean30) continue;

            double zScore = stdDev == 0.0 ? Math.abs(actual - mean30) : Math.abs(actual - mean30) / stdDev;
            if (zScore < 2.0) continue;

            double mean7 = average(series.subList(Math.max(0, series.size() - 8), series.size() - 1));
            double deviation = mean30 == 0.0 ? actual * 100.0 : ((actual - mean30) / mean30) * 100.0;
            double confidence = clamp(50.0 + (zScore - 2.0) * 18.0 + Math.min(20.0, baselineWindow.stream().mapToLong(Long::longValue).sum()), 50.0, 96.0);
            String direction = actual > mean30 ? "ABOVE_BASELINE" : "BELOW_BASELINE";

            anomalies.add(AnomalyResponse.builder()
                    .districtId(district.getId())
                    .districtName(district.getName())
                    .date(LocalDate.now())
                    .expectedValue(round(mean30))
                    .actualValue(round(actual))
                    .rolling7DayAverage(round(mean7))
                    .rolling30DayAverage(round(mean30))
                    .standardDeviation(round(stdDev))
                    .anomalyScore(round(zScore))
                    .deviationPercentage(round(deviation))
                    .confidence(round(confidence))
                    .direction(direction)
                    .explanation("Today's incident count is " + round(Math.abs(deviation))
                            + "% " + (actual > mean30 ? "above" : "below")
                            + " the 30-day baseline and exceeds the 2 standard deviation control band.")
                    .build());
        }

        return anomalies.stream()
                .sorted(Comparator.comparing(AnomalyResponse::getAnomalyScore).reversed())
                .collect(Collectors.toList());
    }

    public List<ForecastResponse> forecastAllDistricts(int forecastDays) {
        return districtRepository.findAll().stream()
                .map(district -> forecastDistrict(district.getId(), forecastDays))
                .sorted(Comparator.comparing(ForecastResponse::getPredictedIncidents).reversed())
                .collect(Collectors.toList());
    }

    public ForecastResponse forecastDistrict(Long districtId, int forecastDays) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));
        Map<LocalDate, Long> dailyCounts = toDateCountMap(
                incidentRepository.getDailyIncidentCountsForDistrict(districtId, LocalDateTime.now().minusDays(HISTORY_DAYS)));
        List<Long> series = completeSeries(dailyCounts, HISTORY_DAYS);
        double baseline = average(series.subList(Math.max(0, series.size() - 30), series.size()));
        double weighted = weightedMovingAverage(series.subList(Math.max(0, series.size() - 14), series.size()));
        double first14 = average(series.subList(Math.max(0, series.size() - 28), Math.max(1, series.size() - 14)));
        double last14 = average(series.subList(Math.max(0, series.size() - 14), series.size()));
        double trendAcceleration = first14 == 0.0 ? (last14 > 0.0 ? 100.0 : 0.0) : ((last14 - first14) / first14) * 100.0;
        Map<DayOfWeek, Double> weekdayFactors = weekdayFactors(dailyCounts, baseline);
        double confidence = clamp(42.0 + Math.min(35.0, series.stream().mapToLong(Long::longValue).sum() * 0.7)
                - Math.min(18.0, Math.abs(trendAcceleration) * 0.12), 35.0, 90.0);

        List<ForecastResponse.ForecastPoint> points = new ArrayList<>();
        double total = 0.0;
        for (int day = 1; day <= forecastDays; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            double accelerationFactor = 1.0 + clamp(trendAcceleration / 100.0, -0.35, 0.45) * (day / (double) Math.max(7, forecastDays));
            double seasonalFactor = weekdayFactors.getOrDefault(date.getDayOfWeek(), 1.0);
            double predicted = Math.max(0.0, ((weighted * 0.65) + (baseline * 0.35)) * accelerationFactor * seasonalFactor);
            double interval = Math.max(1.0, predicted * (1.0 - confidence / 125.0));
            total += predicted;
            points.add(ForecastResponse.ForecastPoint.builder()
                    .date(date)
                    .predictedIncidents(round(predicted))
                    .lowerBound(round(Math.max(0.0, predicted - interval)))
                    .upperBound(round(predicted + interval))
                    .weekdaySeasonalityFactor(round(seasonalFactor))
                    .confidence(round(confidence))
                    .build());
        }

        return ForecastResponse.builder()
                .districtId(districtId)
                .districtName(district.getName())
                .forecastWindowDays(forecastDays)
                .baselineDailyAverage(round(baseline))
                .weightedMovingAverage(round(weighted))
                .trendAcceleration(round(trendAcceleration))
                .confidence(round(confidence))
                .predictedIncidents((int) Math.round(total))
                .forecast(points)
                .explanation("Forecast uses 30-day baseline, weighted 14-day moving average, "
                        + round(trendAcceleration) + "% 14-day trend acceleration, and weekday seasonality.")
                .build();
    }

    public List<SpatialRiskResponse> spatialRisk() {
        List<District> districts = districtRepository.findAll().stream()
                .sorted(Comparator.comparing(District::getId))
                .collect(Collectors.toList());
        Map<Long, Long> recentCounts = countMap(incidentRepository.countIncidentsByDistrictGrouped(LocalDateTime.now().minusDays(7)));
        List<SpatialRiskResponse> responses = new ArrayList<>();

        for (int index = 0; index < districts.size(); index++) {
            District district = districts.get(index);
            List<District> neighbors = adjacentByOrder(districts, index);
            double localDensity = density(recentCounts.getOrDefault(district.getId(), 0L), district.getPopulation());
            double neighborDensity = neighbors.stream()
                    .mapToDouble(neighbor -> density(recentCounts.getOrDefault(neighbor.getId(), 0L), neighbor.getPopulation()))
                    .average()
                    .orElse(0.0);
            double spread = localDensity == 0.0 ? neighborDensity : neighborDensity / Math.max(localDensity, 0.01);
            double propagation = clamp((neighborDensity * 12.0) + (spread * 18.0), 0.0, 100.0);
            double influence = clamp((localDensity * 14.0) + (district.getOperationalRiskScore() * 0.35), 0.0, 100.0);
            double spatialRisk = clamp((influence * 0.65) + (propagation * 0.35), 0.0, 100.0);

            responses.add(SpatialRiskResponse.builder()
                    .districtId(district.getId())
                    .districtName(district.getName())
                    .localIncidentDensity(round(localDensity))
                    .neighboringDistrictDensity(round(neighborDensity))
                    .hotspotSpreadFactor(round(spread))
                    .districtInfluenceScore(round(influence))
                    .neighboringRiskPropagationScore(round(propagation))
                    .spatialRisk(round(spatialRisk))
                    .influenceRadius("ADJACENT_DISTRICTS")
                    .neighboringImpact(round(spatialRisk - influence))
                    .neighboringDistricts(neighbors.stream().map(District::getName).collect(Collectors.toList()))
                    .explanation("No PostGIS boundaries were found, so spatial risk uses deterministic adjacent-district relationships as the first spatial model.")
                    .build());
        }

        return responses.stream()
                .sorted(Comparator.comparing(SpatialRiskResponse::getSpatialRisk).reversed())
                .collect(Collectors.toList());
    }

    public List<RiskExplanationResponse> riskExplanations() {
        return districtRepository.findAll().stream()
                .map(district -> riskExplanation(district.getId()))
                .sorted(Comparator.comparing(RiskExplanationResponse::getRiskScore).reversed())
                .collect(Collectors.toList());
    }

    public RiskExplanationResponse riskExplanation(Long districtId) {
        var analysis = riskScoringService.analyzeDistrictRisk(districtId);
        ForecastResponse forecast = forecastDistrict(districtId, 7);
        List<RiskExplanationResponse.RiskFactor> factors = List.of(
                factor("Incident density", analysis.getIncidentDensityFactor(), 0.30,
                        "Volume of incidents relative to the configured district baseline."),
                factor("Unresolved backlog", analysis.getUnresolvedRatioFactor(), 0.25,
                        "Share of incidents still reported, open, or in progress."),
                factor("Severity mix", analysis.getAverageSeverityFactor(), 0.25,
                        "Weighted average of incident severity from low to critical."),
                factor("Sustainability pressure", analysis.getSustainabilityImpactFactor(), 0.20,
                        "Inverse sustainability score contribution.")
        );
        String trend = forecast.getTrendAcceleration() > 10 ? "INCREASING" : forecast.getTrendAcceleration() < -10 ? "DECREASING" : "STABLE";
        double confidence = clamp(50.0 + Math.min(25.0, analysis.getTotalIncidents()) + forecast.getConfidence() * 0.2, 45.0, 94.0);

        return RiskExplanationResponse.builder()
                .districtId(districtId)
                .districtName(analysis.getDistrictName())
                .riskScore(analysis.getRiskScore())
                .riskLevel(analysis.getRiskLevel())
                .trendDirection(trend)
                .confidence(round(confidence))
                .contributingFactors(factors)
                .explanation("Risk is a weighted blend of incident density, unresolved backlog, severity, and sustainability pressure. Trend comes from the 7-day forecast.")
                .build();
    }

    public List<RecommendationExplanationResponse> recommendationExplanations() {
        Map<Long, Double> anomalyScores = anomalyScoresByDistrict();
        return recommendationRepository.findAll().stream()
                .map(recommendation -> recommendationExplanation(recommendation, anomalyScores))
                .sorted(Comparator.comparing(RecommendationExplanationResponse::getConfidence).reversed())
                .collect(Collectors.toList());
    }

    public List<RecommendationExplanationResponse> recommendationExplanations(Long districtId) {
        Map<Long, Double> anomalyScores = anomalyScoresByDistrict();
        return recommendationRepository.findByDistrict_Id(districtId, Pageable.unpaged()).getContent().stream()
                .map(recommendation -> recommendationExplanation(recommendation, anomalyScores))
                .sorted(Comparator.comparing(RecommendationExplanationResponse::getConfidence).reversed())
                .collect(Collectors.toList());
    }

    private RecommendationExplanationResponse recommendationExplanation(
            Recommendation recommendation,
            Map<Long, Double> anomalyScores) {
        Long districtId = recommendation.getDistrict().getId();
        RiskExplanationResponse risk = riskExplanation(districtId);
        ForecastResponse forecast = forecastDistrict(districtId, 7);
        double anomalyScore = anomalyScores.getOrDefault(districtId, 0.0);
        double confidence = clamp((risk.getConfidence() * 0.45) + (forecast.getConfidence() * 0.35)
                + Math.min(20.0, anomalyScore * 5.0), 35.0, 96.0);
        String impact = risk.getRiskScore() >= 70 || anomalyScore >= 3 ? "high" : risk.getRiskScore() >= 45 ? "medium" : "low";
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("riskScore", risk.getRiskScore());
        evidence.put("riskTrend", risk.getTrendDirection());
        evidence.put("predictedIncidents7d", forecast.getPredictedIncidents());
        evidence.put("trendAcceleration", forecast.getTrendAcceleration());
        evidence.put("anomalyScore", round(anomalyScore));

        return RecommendationExplanationResponse.builder()
                .recommendationId(recommendation.getId())
                .districtId(districtId)
                .districtName(recommendation.getDistrict().getName())
                .type(recommendation.getType())
                .priority(recommendation.getPriority().name())
                .reasoning("Priority is justified by " + risk.getRiskLevel().toLowerCase()
                        + " district risk, " + forecast.getPredictedIncidents()
                        + " forecast incidents over 7 days, and anomaly score " + round(anomalyScore) + ".")
                .impact(impact)
                .confidence(round(confidence))
                .evidence(evidence)
                .build();
    }

    private Map<Long, Double> anomalyScoresByDistrict() {
        return detectIncidentAnomalies().stream()
                .collect(Collectors.toMap(
                        AnomalyResponse::getDistrictId,
                        AnomalyResponse::getAnomalyScore,
                        Math::max));
    }

    private RiskExplanationResponse.RiskFactor factor(String name, double value, double weight, String explanation) {
        return RiskExplanationResponse.RiskFactor.builder()
                .name(name)
                .value(round(value))
                .weight(weight)
                .contribution(round(value * weight))
                .explanation(explanation)
                .build();
    }

    private Map<Long, Map<LocalDate, Long>> loadDailySeriesByDistrict(int days) {
        Map<Long, Map<LocalDate, Long>> result = new HashMap<>();
        for (Object[] row : incidentRepository.getDailyIncidentCountsByDistrict(LocalDateTime.now().minusDays(days))) {
            Long districtId = ((Number) row[0]).longValue();
            LocalDate date = ((java.sql.Date) row[1]).toLocalDate();
            Long count = ((Number) row[2]).longValue();
            result.computeIfAbsent(districtId, ignored -> new HashMap<>()).put(date, count);
        }
        return result;
    }

    private Map<LocalDate, Long> toDateCountMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((java.sql.Date) row[0]).toLocalDate(), ((Number) row[1]).longValue());
        }
        return map;
    }

    private List<Long> completeSeries(Map<LocalDate, Long> counts, int days) {
        List<Long> series = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            series.add(counts.getOrDefault(LocalDate.now().minusDays(i), 0L));
        }
        return series;
    }

    private Map<DayOfWeek, Double> weekdayFactors(Map<LocalDate, Long> counts, double baseline) {
        Map<DayOfWeek, List<Long>> grouped = new HashMap<>();
        counts.forEach((date, count) -> grouped.computeIfAbsent(date.getDayOfWeek(), ignored -> new ArrayList<>()).add(count));
        Map<DayOfWeek, Double> factors = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            factors.put(day, baseline == 0.0 ? 1.0 : clamp(average(grouped.getOrDefault(day, List.of())) / Math.max(0.1, baseline), 0.65, 1.45));
        }
        return factors;
    }

    private List<District> adjacentByOrder(List<District> districts, int index) {
        List<District> neighbors = new ArrayList<>();
        if (index > 0) neighbors.add(districts.get(index - 1));
        if (index < districts.size() - 1) neighbors.add(districts.get(index + 1));
        return neighbors;
    }

    private Map<Long, Long> countMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    private double density(long count, int population) {
        return population <= 0 ? 0.0 : (count * 1000.0) / population;
    }

    private double weightedMovingAverage(List<Long> values) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (int i = 0; i < values.size(); i++) {
            double weight = i + 1;
            weightedSum += values.get(i) * weight;
            weightTotal += weight;
        }
        return weightTotal == 0.0 ? 0.0 : weightedSum / weightTotal;
    }

    private double average(List<Long> values) {
        return values.isEmpty() ? 0.0 : values.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    private double stdDev(List<Long> values, double mean) {
        return Math.sqrt(values.stream().mapToDouble(value -> Math.pow(value - mean, 2)).average().orElse(0.0));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
