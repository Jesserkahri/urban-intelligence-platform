package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.SustainabilityMetricCreateRequest;
import com.urban.intelligence.platform.dto.SustainabilityMetricResponse;
import com.urban.intelligence.platform.dto.SustainabilityScoreResponse;
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
        Page<SustainabilityMetricResponse> response = metricService.getDistrictMetrics(districtId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/metrics/type/{metricType}")
    public ResponseEntity<ApiResponse<Page<SustainabilityMetricResponse>>> getMetricsByType(
            @PathVariable String metricType,
            @PageableDefault(size = 20, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<SustainabilityMetricResponse> response = metricService.getMetricsByType(metricType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SustainabilityMetricResponse>>> getEnvironmentalAlerts() {
        List<SustainabilityMetricResponse> response = metricService.getCriticalMetrics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/scores/calculate/{districtId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<SustainabilityScoreResponse>> calculateDistrictScore(
            @PathVariable Long districtId) {
        SustainabilityScoreResponse response = scoreService.calculateDistrictScore(districtId);
        return ResponseEntity.ok(ApiResponse.success(response, "Sustainability score calculated"));
    }

    @GetMapping("/scores/ranking")
    public ResponseEntity<ApiResponse<List<SustainabilityScoreResponse>>> getRanking() {
        List<SustainabilityScoreResponse> response = scoreService.getRanking();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/scores/improving")
    public ResponseEntity<ApiResponse<List<SustainabilityScoreResponse>>> getImprovingDistricts() {
        List<SustainabilityScoreResponse> response = scoreService.getImprovingDistricts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/scores/declining")
    public ResponseEntity<ApiResponse<List<SustainabilityScoreResponse>>> getDecliningDistricts() {
        List<SustainabilityScoreResponse> response = scoreService.getDecliningDistricts();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
