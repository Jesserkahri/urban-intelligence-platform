package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.analytics.PredictiveIntelligenceService;
import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.predictive.AdvancedAnalyticsResponse;
import com.urban.intelligence.platform.dto.predictive.DistrictRiskForecastResponse;
import com.urban.intelligence.platform.dto.predictive.IncidentForecastResponse;
import com.urban.intelligence.platform.dto.predictive.PredictiveAlertResponse;
import com.urban.intelligence.platform.dto.predictive.PredictiveOverviewResponse;
import com.urban.intelligence.platform.dto.predictive.RecommendationScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictive")
@RequiredArgsConstructor
@Slf4j
public class PredictiveIntelligenceController {

    private final PredictiveIntelligenceService predictiveService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<PredictiveOverviewResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.success(predictiveService.overview()));
    }

    @GetMapping("/incidents/forecast/district/{districtId}")
    public ResponseEntity<ApiResponse<IncidentForecastResponse>> forecastDistrictIncidents(
            @PathVariable Long districtId,
            @RequestParam(defaultValue = "30") int historyDays,
            @RequestParam(defaultValue = "7") int forecastDays) {
        return ResponseEntity.ok(ApiResponse.success(
                predictiveService.forecastDistrictIncidents(districtId, Math.min(historyDays, 180), Math.min(forecastDays, 30))));
    }

    @GetMapping("/incidents/forecast")
    public ResponseEntity<ApiResponse<List<IncidentForecastResponse>>> forecastAllDistrictIncidents(
            @RequestParam(defaultValue = "30") int historyDays,
            @RequestParam(defaultValue = "7") int forecastDays) {
        return ResponseEntity.ok(ApiResponse.success(
                predictiveService.forecastAllDistrictIncidents(Math.min(historyDays, 180), Math.min(forecastDays, 30))));
    }

    @GetMapping("/risk/forecast/district/{districtId}")
    public ResponseEntity<ApiResponse<DistrictRiskForecastResponse>> forecastDistrictRisk(
            @PathVariable Long districtId,
            @RequestParam(defaultValue = "7") int forecastDays) {
        return ResponseEntity.ok(ApiResponse.success(
                predictiveService.forecastDistrictRisk(districtId, Math.min(forecastDays, 30))));
    }

    @GetMapping("/risk/forecast")
    public ResponseEntity<ApiResponse<List<DistrictRiskForecastResponse>>> forecastAllDistrictRisks(
            @RequestParam(defaultValue = "7") int forecastDays) {
        return ResponseEntity.ok(ApiResponse.success(
                predictiveService.forecastAllDistrictRisks(Math.min(forecastDays, 30))));
    }

    @PostMapping("/alerts/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<PredictiveAlertResponse>>> generateAlerts(
            @RequestParam(defaultValue = "7") int forecastDays) {
        return ResponseEntity.ok(ApiResponse.success(
                predictiveService.generatePredictiveAlerts(Math.min(forecastDays, 30)),
                "Predictive alerts generated"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<Page<PredictiveAlertResponse>>> alerts(
            @RequestParam(required = false) String severity,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(predictiveService.alerts(pageable, severity)));
    }

    @PostMapping("/recommendations/score")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<RecommendationScoreResponse>>> scoreRecommendations(
            @RequestParam(required = false) Long districtId) {
        return ResponseEntity.ok(ApiResponse.success(
                predictiveService.scoreRecommendations(districtId),
                "Recommendation scoring completed"));
    }

    @GetMapping("/advanced")
    public ResponseEntity<ApiResponse<AdvancedAnalyticsResponse>> advancedAnalytics(
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(ApiResponse.success(predictiveService.advancedAnalytics(Math.min(days, 365))));
    }
}
