package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.repository.AnalyticsEventRepository;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.domain.repository.RecommendationRepository;
import com.urban.intelligence.platform.dto.analytics.DistrictRiskAnalysisResponse;
import com.urban.intelligence.platform.dto.analytics.DistrictRiskRankingResponse;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DistrictRiskScoringService - Calculates comprehensive district risk metrics
 * 
 * Combines multiple data sources (incidents, sustainability scores, operational data)
 * to produce actionable risk assessments for each district.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DistrictRiskScoringService {

    private final DistrictRepository districtRepository;
    private final IncidentRepository incidentRepository;
    private final RecommendationRepository recommendationRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
    private static final double MAX_INCIDENTS_BASELINE = 100.0;
    private static final Map<Incident.SeverityLevel, Integer> SEVERITY_WEIGHTS = Map.of(
        Incident.SeverityLevel.LOW, 1,
        Incident.SeverityLevel.MEDIUM, 2,
        Incident.SeverityLevel.HIGH, 4,
        Incident.SeverityLevel.CRITICAL, 7
    );

    @Timed(value = "analytics.execution.time", extraTags = {"service", "risk"})
    public DistrictRiskAnalysisResponse analyzeDistrictRisk(Long districtId) {
        log.info("Analyzing risk for district: {}", districtId);

        District district = districtRepository.findById(districtId)
            .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));

        return calculateRiskScore(district, incidentRepository.findByDistrict(district));
    }

    @Timed(value = "analytics.execution.time", extraTags = {"service", "risk"})
    public List<DistrictRiskRankingResponse> getRiskRanking() {
        log.info("Calculating risk ranking for all districts");

        return districtRepository.findAll().stream()
            .map(district -> {
                List<Incident> incidents = incidentRepository.findByDistrict(district);
                DistrictRiskAnalysisResponse analysis = calculateRiskScore(district, incidents);
                return DistrictRiskRankingResponse.builder()
                    .districtId(district.getId())
                    .districtName(district.getName())
                    .riskScore(analysis.getRiskScore())
                    .riskLevel(analysis.getRiskLevel())
                    .incidentCount(incidents.size())
                    .unresolvedCount((int) incidents.stream().filter(this::isUnresolved).count())
                    .population(district.getPopulation())
                    .build();
            })
            .sorted(Comparator.comparingDouble(DistrictRiskRankingResponse::getRiskScore).reversed())
            .collect(Collectors.toList());
    }

    public List<DistrictRiskRankingResponse> getDistrictsByRiskLevel(String riskLevel) {
        return getRiskRanking().stream()
            .filter(district -> district.getRiskLevel().equalsIgnoreCase(riskLevel))
            .collect(Collectors.toList());
    }

    /**
     * Calculate comprehensive risk score for a district
     * 
     * Factors included:
     * - Operational risk score (40% weight)
     * - Recent incident frequency (35% weight)
     * - Sustainability metrics (15% weight)
     * - Urgent recommendations ratio (10% weight)
     */
    public Map<String, Object> calculateDistrictRiskScore(Long districtId) {
        log.debug("Calculating comprehensive risk score for district {}", districtId);
        
        District district = districtRepository.findById(districtId)
            .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));
        
        // Component 1: Operational Risk Score (40%)
        Double operationalRiskComponent = district.getOperationalRiskScore() * 0.40;
        
        // Component 2: Recent Incident Frequency (35%)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Long recentIncidents = incidentRepository.countIncidentsByDistrictAndDate(districtId, sevenDaysAgo);
        Double incidentFrequencyScore = Math.min(100.0, recentIncidents * 10.0); // normalize to 0-100
        Double incidentComponent = (incidentFrequencyScore / 100.0) * 35.0;
        
        // Component 3: Sustainability Metrics (15%)
        Double sustainabilityInverse = 100.0 - district.getSustainabilityScore();
        Double sustainabilityComponent = (sustainabilityInverse / 100.0) * 15.0;
        
        // Component 4: Urgent Recommendations Ratio (10%)
        Long totalRecommendations = recommendationRepository.countByDistrict_Id(districtId);
        Long urgentRecommendations = recommendationRepository
            .countByPriority(com.urban.intelligence.platform.domain.entity.Recommendation.Priority.HIGH) +
            recommendationRepository.countByPriority(
                com.urban.intelligence.platform.domain.entity.Recommendation.Priority.CRITICAL);
        
        Double urgentRatio = totalRecommendations > 0 ? 
            (urgentRecommendations.doubleValue() / totalRecommendations.doubleValue()) : 0.0;
        Double urgentComponent = urgentRatio * 10.0;
        
        // Total Risk Score (0-100)
        Double totalRiskScore = operationalRiskComponent + incidentComponent + 
                                sustainabilityComponent + urgentComponent;
        
        // Determine risk level
        String riskLevel = categorizeRisk(totalRiskScore);
        
        Map<String, Object> riskAnalysis = new LinkedHashMap<>();
        riskAnalysis.put("district_id", districtId);
        riskAnalysis.put("district_name", district.getName());
        riskAnalysis.put("total_risk_score", Math.round(totalRiskScore * 100.0) / 100.0);
        riskAnalysis.put("risk_level", riskLevel);
        riskAnalysis.put("components", new LinkedHashMap<>(){{
            put("operational_risk", Math.round(operationalRiskComponent * 100.0) / 100.0);
            put("incident_frequency", Math.round(incidentComponent * 100.0) / 100.0);
            put("sustainability_inverse", Math.round(sustainabilityComponent * 100.0) / 100.0);
            put("urgent_recommendations", Math.round(urgentComponent * 100.0) / 100.0);
        }});
        riskAnalysis.put("recent_incidents_7_days", recentIncidents);
        riskAnalysis.put("sustainability_score", district.getSustainabilityScore());
        
        log.info("District {} risk score calculated: {} ({})", districtId, totalRiskScore, riskLevel);
        return riskAnalysis;
    }

    /**
     * Get all districts sorted by risk score
     */
    public List<Map<String, Object>> getAllDistrictsRankedByRisk() {
        log.debug("Computing risk rankings for all districts");
        
        List<District> allDistricts = districtRepository.findAll();
        List<Map<String, Object>> riskScores = new ArrayList<>();
        
        for (District district : allDistricts) {
            riskScores.add(calculateDistrictRiskScore(district.getId()));
        }
        
        // Sort by total_risk_score descending
        riskScores.sort((a, b) -> 
            ((Double) b.get("total_risk_score")).compareTo((Double) a.get("total_risk_score")));
        
        return riskScores;
    }

    /**
     * Categorize risk level based on score
     */
    private String categorizeRisk(Double score) {
        if (score >= 75) return "CRITICAL";
        if (score >= 50) return "HIGH";
        if (score >= 25) return "MEDIUM";
        return "LOW";
    }

    private DistrictRiskAnalysisResponse calculateRiskScore(District district, List<Incident> incidents) {
        double incidentFactor = Math.min((incidents.size() / MAX_INCIDENTS_BASELINE) * 100, 100.0);
        long unresolved = incidents.stream().filter(this::isUnresolved).count();
        double unresolvedFactor = incidents.isEmpty() ? 0.0 : (unresolved / (double) incidents.size()) * 100;
        double severityFactor = incidents.isEmpty() ? 0.0
            : (incidents.stream()
                .mapToInt(i -> SEVERITY_WEIGHTS.getOrDefault(i.getSeverity(), 1))
                .average()
                .orElse(1.0) / 7.0) * 100;
        double sustainabilityFactor = 100 - district.getSustainabilityScore();

        double riskScore = (incidentFactor * 0.30)
            + (unresolvedFactor * 0.25)
            + (severityFactor * 0.25)
            + (sustainabilityFactor * 0.20);

        return DistrictRiskAnalysisResponse.builder()
            .districtId(district.getId())
            .districtName(district.getName())
            .riskScore(round(Math.min(riskScore, 100.0)))
            .riskLevel(classifyRiskLevel(riskScore))
            .incidentDensityFactor(round(incidentFactor))
            .unresolvedRatioFactor(round(unresolvedFactor))
            .averageSeverityFactor(round(severityFactor))
            .sustainabilityImpactFactor(round(sustainabilityFactor))
            .totalIncidents(incidents.size())
            .unresolvedIncidents((int) unresolved)
            .sustainabilityScore(district.getSustainabilityScore())
            .population(district.getPopulation())
            .build();
    }

    private boolean isUnresolved(Incident incident) {
        return incident.getStatus() == Incident.IncidentStatus.OPEN
            || incident.getStatus() == Incident.IncidentStatus.REPORTED
            || incident.getStatus() == Incident.IncidentStatus.IN_PROGRESS;
    }

    private String classifyRiskLevel(double riskScore) {
        if (riskScore >= 76) return "CRITICAL";
        if (riskScore >= 51) return "HIGH";
        if (riskScore >= 26) return "MODERATE";
        return "LOW";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
