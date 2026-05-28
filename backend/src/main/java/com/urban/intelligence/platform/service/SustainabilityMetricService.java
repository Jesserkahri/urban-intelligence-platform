package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.domain.entity.SustainabilityMetric;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.SustainabilityMetricRepository;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.dto.SustainabilityMetricResponse;
import com.urban.intelligence.platform.dto.SustainabilityMetricCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SustainabilityMetricService - Business logic for sustainability metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SustainabilityMetricService {

    private final SustainabilityMetricRepository metricRepository;
    private final DistrictRepository districtRepository;

    /**
     * Record a new sustainability metric
     */
    @Transactional
    public SustainabilityMetricResponse recordMetric(SustainabilityMetricCreateRequest request) {
        log.info("Recording sustainability metric: {} for district: {}", request.getMetricType(), request.getDistrictId());

        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + request.getDistrictId()));

        SustainabilityMetric metric = SustainabilityMetric.builder()
                .district(district)
                .metricType(request.getMetricType())
                .value(request.getValue())
                .unit(request.getUnit())
                .threshold(request.getThreshold())
                .source(request.getSource())
                .timestamp(LocalDateTime.now())
                .build();

        SustainabilityMetric saved = metricRepository.save(metric);
        log.info("Metric recorded with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Get all metrics for a district
     */
    @Transactional(readOnly = true)
    public Page<SustainabilityMetricResponse> getDistrictMetrics(Long districtId, Pageable pageable) {
        log.debug("Fetching sustainability metrics for district: {}", districtId);
        return metricRepository.findByDistrict_Id(districtId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get metrics by type across all districts
     */
    @Transactional(readOnly = true)
    public Page<SustainabilityMetricResponse> getMetricsByType(String metricType, Pageable pageable) {
        log.debug("Fetching metrics by type: {}", metricType);
        return metricRepository.findByMetricType(metricType, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get critical metrics (alerts)
     */
    @Transactional(readOnly = true)
    public List<SustainabilityMetricResponse> getCriticalMetrics() {
        log.debug("Fetching critical sustainability metrics");
        return metricRepository.findCriticalMetrics().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get recent metrics for a district
     */
    @Transactional(readOnly = true)
    public List<SustainabilityMetricResponse> getRecentMetrics(Long districtId, int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return metricRepository.findRecentMetrics(districtId, since).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get latest value for a specific metric type
     */
    @Transactional(readOnly = true)
    public SustainabilityMetricResponse getLatestMetric(Long districtId, String metricType) {
        SustainabilityMetric metric = metricRepository.findLatestByDistrictAndType(districtId, metricType);
        if (metric == null) {
            throw new IllegalArgumentException("No metric found for district: " + districtId + " type: " + metricType);
        }
        return mapToResponse(metric);
    }

    /**
     * Map entity to response DTO
     */
    private SustainabilityMetricResponse mapToResponse(SustainabilityMetric metric) {
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
}
