package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.entity.PredictiveAlert;
import com.urban.intelligence.platform.domain.entity.Recommendation;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.domain.repository.PredictiveAlertRepository;
import com.urban.intelligence.platform.domain.repository.RecommendationRepository;
import com.urban.intelligence.platform.dto.predictive.AdvancedAnalyticsResponse;
import com.urban.intelligence.platform.dto.predictive.DistrictRiskForecastResponse;
import com.urban.intelligence.platform.dto.predictive.IncidentForecastResponse;
import com.urban.intelligence.platform.dto.predictive.PredictiveAlertResponse;
import com.urban.intelligence.platform.dto.predictive.PredictiveOverviewResponse;
import com.urban.intelligence.platform.dto.predictive.RecommendationScoreResponse;
import com.urban.intelligence.platform.realtime.RealTimeOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictiveIntelligenceService {

    private static final int DEFAULT_HISTORY_DAYS = 30;
    private static final int DEFAULT_FORECAST_DAYS = 7;

    private final IncidentRepository incidentRepository;
    private final DistrictRepository districtRepository;
    private final RecommendationRepository recommendationRepository;
    private final PredictiveAlertRepository predictiveAlertRepository;
    private final DistrictRiskScoringService riskScoringService;
    private final RealTimeOperationsService realTimeOperationsService;

    @Transactional(readOnly = true)
    public IncidentForecastResponse forecastDistrictIncidents(Long districtId, int historyDays, int forecastDays) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));
        List<Incident> incidents = incidentRepository.findByDistrictAndCreatedAtAfter(
                district,
                LocalDateTime.now().minusDays(historyDays));
        return buildIncidentForecast(district, incidents, historyDays, forecastDays);
    }

    @Transactional(readOnly = true)
    public List<IncidentForecastResponse> forecastAllDistrictIncidents(int historyDays, int forecastDays) {
        return districtRepository.findAll().stream()
                .map(district -> buildIncidentForecast(
                        district,
                        incidentRepository.findByDistrictAndCreatedAtAfter(district, LocalDateTime.now().minusDays(historyDays)),
                        historyDays,
                        forecastDays))
                .sorted(Comparator.comparing(IncidentForecastResponse::getPredictedIncidentCount).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DistrictRiskForecastResponse forecastDistrictRisk(Long districtId, int forecastDays) {
        District district = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));
        IncidentForecastResponse incidentForecast = forecastDistrictIncidents(districtId, DEFAULT_HISTORY_DAYS, forecastDays);
        double currentRisk = riskScoringService.analyzeDistrictRisk(districtId).getRiskScore();
        double growthPressure = Math.max(0.0, incidentForecast.getGrowthRatePercentage()) * 0.25;
        double incidentPressure = Math.min(20.0, incidentForecast.getPredictedIncidentCount() * 1.5);
        double predictedRisk = clamp(currentRisk + growthPressure + incidentPressure - 5.0, 0.0, 100.0);

        return DistrictRiskForecastResponse.builder()
                .districtId(districtId)
                .districtName(district.getName())
                .currentRiskScore(round(currentRisk))
                .predictedRiskScore(round(predictedRisk))
                .currentRiskLevel(riskLevel(currentRisk))
                .predictedRiskLevel(riskLevel(predictedRisk))
                .riskDelta(round(predictedRisk - currentRisk))
                .driverSummary(buildRiskDriverSummary(incidentForecast, currentRisk, predictedRisk))
                .confidence(incidentForecast.getConfidence())
                .forecastWindowDays(forecastDays)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DistrictRiskForecastResponse> forecastAllDistrictRisks(int forecastDays) {
        return districtRepository.findAll().stream()
                .map(district -> forecastDistrictRisk(district.getId(), forecastDays))
                .sorted(Comparator.comparing(DistrictRiskForecastResponse::getPredictedRiskScore).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PredictiveAlertResponse> generatePredictiveAlerts(int forecastDays) {
        List<PredictiveAlertResponse> responses = new ArrayList<>();
        for (IncidentForecastResponse forecast : forecastAllDistrictIncidents(DEFAULT_HISTORY_DAYS, forecastDays)) {
            double baselineTotal = forecast.getBaselineDailyAverage() * forecastDays;
            if (forecast.getPredictedIncidentCount() >= Math.max(3.0, baselineTotal * 1.35)
                    && forecast.getConfidence() >= 45.0) {
                responses.add(saveAlert(
                        forecast.getDistrictId(),
                        "INCIDENT_GROWTH_WARNING",
                        severityFromProbability(forecast.getConfidence()),
                        "Incident growth likely",
                        "Forecast predicts " + forecast.getPredictedIncidentCount() + " incidents in " + forecastDays
                                + " days for " + forecast.getDistrictName() + ".",
                        probabilityFromGrowth(forecast.getGrowthRatePercentage()),
                        forecast.getConfidence(),
                        forecastDays,
                        (double) forecast.getPredictedIncidentCount(),
                        baselineTotal));
            }
        }

        for (DistrictRiskForecastResponse risk : forecastAllDistrictRisks(forecastDays)) {
            if (("HIGH".equals(risk.getPredictedRiskLevel()) || "CRITICAL".equals(risk.getPredictedRiskLevel()))
                    && risk.getRiskDelta() >= 8.0) {
                responses.add(saveAlert(
                        risk.getDistrictId(),
                        "RISK_ESCALATION_WARNING",
                        "CRITICAL".equals(risk.getPredictedRiskLevel()) ? "CRITICAL" : "HIGH",
                        "Operational risk escalation likely",
                        risk.getDistrictName() + " may rise to " + risk.getPredictedRiskLevel()
                                + " risk. " + risk.getDriverSummary(),
                        clamp(55.0 + risk.getRiskDelta(), 0.0, 95.0),
                        risk.getConfidence(),
                        forecastDays,
                        risk.getPredictedRiskScore(),
                        risk.getCurrentRiskScore()));
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public Page<PredictiveAlertResponse> alerts(Pageable pageable, String severity) {
        return (severity == null || severity.isBlank()
                ? predictiveAlertRepository.findAll(pageable)
                : predictiveAlertRepository.findBySeverity(severity.toUpperCase(), pageable))
                .map(this::mapAlert);
    }

    @Transactional
    public List<RecommendationScoreResponse> scoreRecommendations(Long districtId) {
        List<Recommendation> recommendations = districtId == null
                ? recommendationRepository.findAll()
                : recommendationRepository.findByDistrict_Id(districtId, Pageable.unpaged()).getContent();

        List<RecommendationScoreResponse> responses = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            District district = recommendation.getDistrict();
            double districtRisk = riskScoringService.analyzeDistrictRisk(district.getId()).getRiskScore();
            double priorityWeight = switch (recommendation.getPriority()) {
                case CRITICAL -> 95.0;
                case HIGH -> 80.0;
                case MEDIUM -> 60.0;
                case LOW -> 35.0;
            };
            double typeEffectiveness = effectivenessForType(recommendation.getType());
            double predictedImpact = clamp((districtRisk * 0.45) + (priorityWeight * 0.35) + (typeEffectiveness * 0.20), 0.0, 100.0);
            double confidence = clamp(45.0 + (recommendation.getAutoGenerated() ? 10.0 : 0.0) + (districtRisk / 5.0), 35.0, 92.0);

            recommendation.setPredictedImpact(round(predictedImpact));
            recommendation.setInterventionEffectiveness(round(typeEffectiveness));
            recommendation.setOperationalConfidence(round(confidence));
            Recommendation saved = recommendationRepository.save(recommendation);

            responses.add(RecommendationScoreResponse.builder()
                    .recommendationId(saved.getId())
                    .districtId(district.getId())
                    .districtName(district.getName())
                    .type(saved.getType())
                    .priority(saved.getPriority().name())
                    .predictedImpact(saved.getPredictedImpact())
                    .interventionEffectiveness(saved.getInterventionEffectiveness())
                    .operationalConfidence(saved.getOperationalConfidence())
                    .scoringRationale("Impact combines district risk, priority urgency, and intervention type effectiveness.")
                    .build());
        }
        return responses.stream()
                .sorted(Comparator.comparing(RecommendationScoreResponse::getPredictedImpact).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdvancedAnalyticsResponse advancedAnalytics(int days) {
        List<Incident> incidents = incidentRepository.findByCreatedAtAfter(LocalDateTime.now().minusDays(days));
        Map<DayOfWeek, Double> seasonal = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            double avg = incidents.stream()
                    .filter(incident -> incident.getCreatedAt().getDayOfWeek() == day)
                    .count() / Math.max(1.0, days / 7.0);
            seasonal.put(day, round(avg));
        }

        Map<String, Long> typeCounts = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getType, Collectors.counting()));
        double mean = typeCounts.values().stream().mapToLong(Long::longValue).average().orElse(0.0);
        double std = Math.sqrt(typeCounts.values().stream()
                .mapToDouble(count -> Math.pow(count - mean, 2))
                .average()
                .orElse(0.0));
        List<String> recurringAnomalies = typeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > mean + std && entry.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        double anomalyPressure = mean == 0.0 ? 0.0 : clamp((std / mean) * 50.0, 0.0, 100.0);

        Map<String, Double> dayMap = new LinkedHashMap<>();
        seasonal.forEach((day, value) -> dayMap.put(day.name(), value));
        String peakDay = dayMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");

        return AdvancedAnalyticsResponse.builder()
                .analysisWindowDays(days)
                .dayOfWeekSeasonality(dayMap)
                .recurringAnomalyCategories(recurringAnomalies)
                .anomalyPressureScore(round(anomalyPressure))
                .seasonalPatternSummary("Highest observed incident pressure occurs on " + peakDay + ".")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public PredictiveOverviewResponse overview() {
        List<IncidentForecastResponse> forecasts = forecastAllDistrictIncidents(DEFAULT_HISTORY_DAYS, DEFAULT_FORECAST_DAYS).stream()
                .limit(5)
                .collect(Collectors.toList());
        List<DistrictRiskForecastResponse> risks = forecastAllDistrictRisks(DEFAULT_FORECAST_DAYS).stream()
                .limit(5)
                .collect(Collectors.toList());
        List<PredictiveAlertResponse> warnings = generatePredictiveAlerts(DEFAULT_FORECAST_DAYS);
        return PredictiveOverviewResponse.builder()
                .incidentForecasts(forecasts)
                .riskForecasts(risks)
                .earlyWarnings(warnings)
                .advancedAnalytics(advancedAnalytics(60))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private IncidentForecastResponse buildIncidentForecast(District district, List<Incident> incidents, int historyDays, int forecastDays) {
        Map<LocalDate, Long> counts = incidents.stream()
                .collect(Collectors.groupingBy(incident -> incident.getCreatedAt().toLocalDate(), Collectors.counting()));
        List<Long> series = new ArrayList<>();
        for (int i = historyDays - 1; i >= 0; i--) {
            series.add(counts.getOrDefault(LocalDate.now().minusDays(i), 0L));
        }
        double baseline = series.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double firstHalf = series.subList(0, Math.max(1, series.size() / 2)).stream().mapToLong(Long::longValue).average().orElse(0.0);
        double secondHalf = series.subList(series.size() / 2, series.size()).stream().mapToLong(Long::longValue).average().orElse(0.0);
        double growth = firstHalf == 0.0 ? (secondHalf > 0 ? 100.0 : 0.0) : ((secondHalf - firstHalf) / firstHalf) * 100;
        double dailyGrowth = clamp(growth / 100.0 / Math.max(1, forecastDays), -0.25, 0.35);
        double confidence = clamp(35.0 + Math.min(40.0, incidents.size() * 2.0) - Math.abs(growth) * 0.1, 30.0, 92.0);

        List<IncidentForecastResponse.ForecastPoint> points = new ArrayList<>();
        double total = 0.0;
        for (int day = 1; day <= forecastDays; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            double weekdayMultiplier = weekdayMultiplier(incidents, date.getDayOfWeek(), baseline);
            double predicted = Math.max(0.0, baseline * Math.pow(1.0 + dailyGrowth, day) * weekdayMultiplier);
            total += predicted;
            double spread = Math.max(1.0, predicted * (1.0 - confidence / 120.0));
            points.add(IncidentForecastResponse.ForecastPoint.builder()
                    .date(date)
                    .predictedCount(round(predicted))
                    .lowerBound(round(Math.max(0.0, predicted - spread)))
                    .upperBound(round(predicted + spread))
                    .build());
        }

        return IncidentForecastResponse.builder()
                .districtId(district.getId())
                .districtName(district.getName())
                .historyWindowDays(historyDays)
                .forecastWindowDays(forecastDays)
                .baselineDailyAverage(round(baseline))
                .growthRatePercentage(round(growth))
                .predictedIncidentCount((int) Math.round(total))
                .trendDirection(growth > 10 ? "INCREASING" : growth < -10 ? "DECREASING" : "STABLE")
                .confidence(round(confidence))
                .forecast(points)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private double weekdayMultiplier(List<Incident> incidents, DayOfWeek dayOfWeek, double baseline) {
        if (baseline <= 0.0) return 1.0;
        long matching = incidents.stream().filter(incident -> incident.getCreatedAt().getDayOfWeek() == dayOfWeek).count();
        double dayAverage = matching / Math.max(1.0, incidents.size() / 7.0);
        return clamp(0.75 + (dayAverage / Math.max(1.0, baseline)) * 0.25, 0.75, 1.35);
    }

    private PredictiveAlertResponse saveAlert(Long districtId, String type, String severity, String title, String message,
                                              double probability, double confidence, int windowDays,
                                              Double predictedValue, Double baselineValue) {
        District district = districtId == null ? null : districtRepository.findById(districtId).orElse(null);
        PredictiveAlert saved = predictiveAlertRepository.save(PredictiveAlert.builder()
                .district(district)
                .alertType(type)
                .severity(severity)
                .title(title)
                .message(message)
                .probability(round(probability))
                .confidence(round(confidence))
                .forecastWindowDays(windowDays)
                .predictedValue(round(predictedValue))
                .baselineValue(round(baselineValue))
                .build());

        if ("HIGH".equals(severity) || "CRITICAL".equals(severity)) {
            realTimeOperationsService.createAlertNotification(type, severity, title, message, null, districtId);
        }
        return mapAlert(saved);
    }

    private PredictiveAlertResponse mapAlert(PredictiveAlert alert) {
        return PredictiveAlertResponse.builder()
                .id(alert.getId())
                .districtId(alert.getDistrict() == null ? null : alert.getDistrict().getId())
                .districtName(alert.getDistrict() == null ? null : alert.getDistrict().getName())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .probability(alert.getProbability())
                .confidence(alert.getConfidence())
                .forecastWindowDays(alert.getForecastWindowDays())
                .predictedValue(alert.getPredictedValue())
                .baselineValue(alert.getBaselineValue())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private String buildRiskDriverSummary(IncidentForecastResponse forecast, double currentRisk, double predictedRisk) {
        return "Risk moves from " + riskLevel(currentRisk) + " to " + riskLevel(predictedRisk)
                + " with " + forecast.getTrendDirection().toLowerCase() + " incident pressure.";
    }

    private String riskLevel(double score) {
        if (score >= 76) return "CRITICAL";
        if (score >= 51) return "HIGH";
        if (score >= 26) return "MODERATE";
        return "LOW";
    }

    private double effectivenessForType(String type) {
        String normalized = type == null ? "" : type.toUpperCase();
        if (normalized.contains("EMERGENCY")) return 90.0;
        if (normalized.contains("TRAFFIC")) return 82.0;
        if (normalized.contains("INFRASTRUCTURE")) return 76.0;
        if (normalized.contains("RESOURCE")) return 72.0;
        if (normalized.contains("SUSTAINABILITY")) return 65.0;
        if (normalized.contains("PROCESS")) return 58.0;
        return 55.0;
    }

    private String severityFromProbability(double probability) {
        if (probability >= 80) return "CRITICAL";
        if (probability >= 60) return "HIGH";
        if (probability >= 40) return "MEDIUM";
        return "LOW";
    }

    private double probabilityFromGrowth(double growth) {
        return clamp(40.0 + growth * 0.7, 0.0, 95.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }

}


