package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.dto.analytics.HotspotRankingResponse;
import com.urban.intelligence.platform.dto.analytics.HotspotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HotspotDetectionService - Identifies geographic incident hotspots
 * 
 * Analyzes spatial clusters of incidents to identify high-risk areas
 * that require increased monitoring or intervention.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class HotspotDetectionService {

    private final IncidentRepository incidentRepository;
    private final DistrictRepository districtRepository;

    private static final double HOTSPOT_THRESHOLD = 5.0; // incidents per square unit
    private static final double CLUSTER_RADIUS = 0.05; // approximate geographic radius
    private static final Map<Incident.SeverityLevel, Integer> SEVERITY_WEIGHTS = Map.of(
        Incident.SeverityLevel.LOW, 1,
        Incident.SeverityLevel.MEDIUM, 2,
        Incident.SeverityLevel.HIGH, 4,
        Incident.SeverityLevel.CRITICAL, 7
    );
    private static final List<Incident.IncidentStatus> UNRESOLVED_STATUSES = List.of(
        Incident.IncidentStatus.REPORTED,
        Incident.IncidentStatus.IN_PROGRESS
    );

    public HotspotResponse detectDistrictHotspot(Long districtId) {
        log.info("Detecting hotspot for district: {}", districtId);

        District district = districtRepository.findById(districtId)
            .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtId));

        List<Incident> incidents = incidentRepository.findByDistrictAndStatusIn(district, UNRESOLVED_STATUSES);
        return calculateHotspotMetrics(district, incidents);
    }

    public List<HotspotResponse> detectAllHotspots() {
        log.info("Detecting hotspots across all districts");

        return districtRepository.findAll().stream()
            .map(district -> calculateHotspotMetrics(
                district,
                incidentRepository.findByDistrictAndStatusIn(district, UNRESOLVED_STATUSES)))
            .filter(hotspot -> hotspot.getHotspotScore() > 0)
            .sorted(Comparator.comparingDouble(HotspotResponse::getHotspotScore).reversed())
            .collect(Collectors.toList());
    }

    public List<HotspotRankingResponse> getTopCriticalHotspots(int limit) {
        log.info("Fetching top {} critical hotspots", limit);

        List<HotspotResponse> hotspots = detectAllHotspots().stream()
            .limit(limit)
            .collect(Collectors.toList());

        List<HotspotRankingResponse> ranking = new ArrayList<>();
        for (int i = 0; i < hotspots.size(); i++) {
            HotspotResponse hotspot = hotspots.get(i);
            ranking.add(HotspotRankingResponse.builder()
                .rank(i + 1)
                .districtName(hotspot.getDistrictName())
                .hotspotScore(hotspot.getHotspotScore())
                .criticalityLevel(determineCriticality(hotspot.getHotspotScore()))
                .unresolvedIncidents(hotspot.getUnresolvedIncidentCount())
                .averageSeverity(hotspot.getAverageSeverity())
                .build());
        }
        return ranking;
    }

    /**
     * Detect geographic incident hotspots
     * 
     * @return Map of hotspot coordinates to incident counts
     */
    public Map<String, Integer> detectIncidentHotspots() {
        log.info("Starting incident hotspot detection");
        
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<Incident> recentIncidents = incidentRepository.findByCreatedAtAfter(twentyFourHoursAgo);
        
        if (recentIncidents.isEmpty()) {
            log.debug("No recent incidents found for hotspot detection");
            return new HashMap<>();
        }

        Map<String, Integer> hotspots = new HashMap<>();
        
        // Cluster incidents by proximity
        for (Incident incident : recentIncidents) {
            String clusterKey = generateClusterKey(incident.getLatitude(), incident.getLongitude());
            hotspots.put(clusterKey, hotspots.getOrDefault(clusterKey, 0) + 1);
        }
        
        // Filter to only high-density clusters
        Map<String, Integer> significantHotspots = hotspots.entrySet().stream()
            .filter(entry -> entry.getValue() >= HOTSPOT_THRESHOLD)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        log.info("Detected {} hotspot clusters from {} recent incidents", 
                 significantHotspots.size(), recentIncidents.size());
        
        return significantHotspots;
    }

    /**
     * Get districts with highest incident concentration
     */
    public List<Map<String, Object>> getHighRiskDistricts() {
        log.debug("Analyzing district-level incident concentrations");
        
        List<District> allDistricts = districtRepository.findAll();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        List<Map<String, Object>> riskAnalysis = new ArrayList<>();
        
        for (District district : allDistricts) {
            Long incidentCount = incidentRepository.countIncidentsByDistrictAndDate(district.getId(), sevenDaysAgo);
            if (incidentCount > 0) {
                Map<String, Object> analysis = new LinkedHashMap<>();
                analysis.put("district_id", district.getId());
                analysis.put("district_name", district.getName());
                analysis.put("incident_count_7_days", incidentCount);
                analysis.put("operational_risk_score", district.getOperationalRiskScore());
                
                // Calculate incident density (incidents per 1000 population)
                Double incidentDensity = (incidentCount * 1000.0) / district.getPopulation();
                analysis.put("incident_density", Math.round(incidentDensity * 100.0) / 100.0);
                
                riskAnalysis.add(analysis);
            }
        }
        
        // Sort by incident density
        riskAnalysis.sort((a, b) -> 
            ((Double) b.get("incident_density")).compareTo((Double) a.get("incident_density")));
        
        return riskAnalysis;
    }

    /**
     * Generate geographic cluster key for incident proximity
     */
    private String generateClusterKey(Double lat, Double lon) {
        // Round to nearest cluster radius to group nearby incidents
        long latBucket = Math.round(lat / CLUSTER_RADIUS);
        long lonBucket = Math.round(lon / CLUSTER_RADIUS);
        return latBucket + "," + lonBucket;
    }

    private HotspotResponse calculateHotspotMetrics(District district, List<Incident> incidents) {
        if (incidents.isEmpty()) {
            return HotspotResponse.builder()
                .districtId(district.getId())
                .districtName(district.getName())
                .hotspotScore(0.0)
                .incidentCount(0)
                .unresolvedIncidentCount(0)
                .unresolvedRatio(0.0)
                .averageSeverity("NONE")
                .riskIntensity("LOW")
                .build();
        }

        double weightedSum = incidents.stream()
            .mapToInt(incident -> SEVERITY_WEIGHTS.getOrDefault(incident.getSeverity(), 1))
            .sum();
        double hotspotScore = Math.min((weightedSum / incidents.size()) * 10, 100.0);
        long unresolved = incidents.stream()
            .filter(this::isUnresolved)
            .count();

        return HotspotResponse.builder()
            .districtId(district.getId())
            .districtName(district.getName())
            .hotspotScore(round(hotspotScore))
            .incidentCount(incidents.size())
            .unresolvedIncidentCount((int) unresolved)
            .unresolvedRatio(round(unresolved / (double) incidents.size()))
            .averageSeverity(calculateAverageSeverity(incidents))
            .riskIntensity(determineRiskIntensity(hotspotScore))
            .build();
    }

    private boolean isUnresolved(Incident incident) {
        return incident.getStatus() == Incident.IncidentStatus.REPORTED
            || incident.getStatus() == Incident.IncidentStatus.IN_PROGRESS;
    }

    private String determineRiskIntensity(double hotspotScore) {
        if (hotspotScore >= 25) return "CRITICAL";
        if (hotspotScore >= 18) return "HIGH";
        if (hotspotScore >= 10) return "MODERATE";
        return "LOW";
    }

    private String determineCriticality(double hotspotScore) {
        if (hotspotScore >= 80) return "CRITICAL";
        if (hotspotScore >= 60) return "HIGH";
        if (hotspotScore >= 40) return "MODERATE";
        return "LOW";
    }

    private String calculateAverageSeverity(List<Incident> incidents) {
        double avgWeight = incidents.stream()
            .mapToInt(i -> SEVERITY_WEIGHTS.getOrDefault(i.getSeverity(), 1))
            .average()
            .orElse(1.0);

        if (avgWeight >= 5) return Incident.SeverityLevel.CRITICAL.name();
        if (avgWeight >= 3) return Incident.SeverityLevel.HIGH.name();
        if (avgWeight >= 1.5) return Incident.SeverityLevel.MEDIUM.name();
        return Incident.SeverityLevel.LOW.name();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
