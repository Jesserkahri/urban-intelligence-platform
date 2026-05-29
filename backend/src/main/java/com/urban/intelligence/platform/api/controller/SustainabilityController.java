package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.EnvironmentalSummaryResponse;
import com.urban.intelligence.platform.dto.MobilitySummaryResponse;
import com.urban.intelligence.platform.dto.SustainabilityDashboardResponse;
import com.urban.intelligence.platform.dto.SustainabilityMetricCreateRequest;
import com.urban.intelligence.platform.dto.SustainabilityMetricResponse;
import com.urban.intelligence.platform.dto.SustainabilityScoreResponse;
import com.urban.intelligence.platform.dto.SustainabilityTrendResponse;
import com.urban.intelligence.platform.service.SustainabilityMetricService;
import com.urban.intelligence.platform.service.SustainabilityScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sustainability")
@RequiredArgsConstructor
@Slf4j
public class SustainabilityController {

    private final SustainabilityMetricService metricService;
    private final SustainabilityScoreService scoreService;

    @PostMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<SustainabilityMetricResponse>> recordMetric(
            @Valid @RequestBody SustainabilityMetricCreateRequest request) {
        SustainabilityMetricResponse response = metricService.recordMetric(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Sustainability metric recorded"));
    }

    @GetMapping("/districts/{districtId}/metrics")
    public ResponseEntity<ApiResponse<Page<SustainabilityMetricResponse>>> getDistrictMetrics(
            @PathVariable Long districtId,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getDistrictMetrics(districtId, pageable)));
    }

    @GetMapping("/metrics/type/{metricType}")
    public ResponseEntity<ApiResponse<Page<SustainabilityMetricResponse>>> getMetricsByType(
            @PathVariable String metricType,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getMetricsByType(metricType, pageable)));
    }

    @GetMapping("/districts/{districtId}/metrics/recent")
    public ResponseEntity<ApiResponse<List<SustainabilityMetricResponse>>> getRecentMetrics(
            @PathVariable Long districtId,
            @RequestParam(defaultValue = "24") int hoursBack) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getRecentMetrics(districtId, Math.min(hoursBack, 720))));
    }

    @GetMapping("/districts/{districtId}/metrics/{metricType}/latest")
    public ResponseEntity<ApiResponse<SustainabilityMetricResponse>> getLatestMetric(
            @PathVariable Long districtId,
            @PathVariable String metricType) {
        return ResponseEntity.ok(ApiResponse.success(metricService.getLatestMetric(districtId, metricType)));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SustainabilityMetricResponse>>> getEnvironmentalAlerts() {
        return ResponseEntity.ok(ApiResponse.success(metricService.getCriticalMetrics()));
    }

    @PostMapping("/scores/calculate/{districtId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<SustainabilityScoreResponse>> calculateDistrictScore(@PathVariable Long districtId) {
        return ResponseEntity.ok(ApiResponse.success(scoreService.calculateDistrictScore(districtId), "Sustainability score calculated"));
    }

    @PostMapping("/scores/calculate-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<Void>> calculateAllScores() {
        scoreService.calculateAllScores();
        return ResponseEntity.accepted().body(ApiResponse.success(null, "Sustainability scores recalculated"));
    }

    @GetMapping("/scores/ranking")
    public ResponseEntity<ApiResponse<List<SustainabilityScoreResponse>>> getRanking() {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getRanking()));
    }

    @GetMapping("/scores/improving")
    public ResponseEntity<ApiResponse<List<SustainabilityScoreResponse>>> getImprovingDistricts() {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getImprovingDistricts()));
    }

    @GetMapping("/scores/declining")
    public ResponseEntity<ApiResponse<List<SustainabilityScoreResponse>>> getDecliningDistricts() {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getDecliningDistricts()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SustainabilityDashboardResponse>> getOperationsDashboard() {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getOperationsDashboard()));
    }

    @GetMapping("/districts/{districtId}/dashboard")
    public ResponseEntity<ApiResponse<SustainabilityDashboardResponse>> getDistrictDashboard(@PathVariable Long districtId) {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getDistrictDashboard(districtId)));
    }

    @GetMapping("/environmental/summary")
    public ResponseEntity<ApiResponse<EnvironmentalSummaryResponse>> getEnvironmentalSummary(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getEnvironmentalSummary(Math.min(days, 365))));
    }

    @GetMapping("/mobility/summary")
    public ResponseEntity<ApiResponse<MobilitySummaryResponse>> getMobilitySummary(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getMobilitySummary(Math.min(days, 365))));
    }

    @GetMapping("/districts/{districtId}/trends")
    public ResponseEntity<ApiResponse<SustainabilityTrendResponse>> getTrendEvolution(
            @PathVariable Long districtId,
            @RequestParam(defaultValue = "90") int days) {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getTrendEvolution(districtId, Math.min(days, 730))));
    }
}
