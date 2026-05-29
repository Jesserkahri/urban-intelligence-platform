package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.SustainabilityMetric;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.SustainabilityMetricRepository;
import com.urban.intelligence.platform.dto.SustainabilityMetricCreateRequest;
import com.urban.intelligence.platform.dto.SustainabilityMetricResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SustainabilityMetricService - Business logic for sustainability metrics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SustainabilityMetricService {

    private final SustainabilityMetricRepository metricRepository;
    private final DistrictRepository districtRepository;

    @Transactional
    public SustainabilityMetricResponse recordMetric(SustainabilityMetricCreateRequest request) {
        log.info("Recording sustainability metric: {} for district: {}", request.getMetricType(), request.getDistrictId());

        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + request.getDistrictId()));

        SustainabilityMetric metric = SustainabilityMetric.builder()
                .district(district)
                .metricType(request.getMetricType().toUpperCase())
                .value(request.getValue())
                .unit(request.getUnit())
                .threshold(request.getThreshold())
                .source(request.getSource())
                .timestamp(LocalDateTime.now())
                .build();

        SustainabilityMetric saved = metricRepository.save(metric);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<SustainabilityMetricResponse> getDistrictMetrics(Long districtId, Pageable pageable) {
        return metricRepository.findByDistrict_Id(districtId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<SustainabilityMetricResponse> getMetricsByType(String metricType, Pageable pageable) {
        return metricRepository.findByMetricType(metricType.toUpperCase(), pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<SustainabilityMetricResponse> getCriticalMetrics() {
        return metricRepository.findCriticalMetrics().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SustainabilityMetricResponse> getRecentMetrics(Long districtId, int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return metricRepository.findRecentMetrics(districtId, since).stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public SustainabilityMetricResponse getLatestMetric(Long districtId, String metricType) {
        List<SustainabilityMetric> metrics = metricRepository.findLatestByDistrictAndType(
                districtId,
                metricType.toUpperCase(),
                PageRequest.of(0, 1));
        if (metrics.isEmpty()) {
            throw new IllegalArgumentException("No metric found for district: " + districtId + " type: " + metricType);
        }
        return mapToResponse(metrics.get(0));
    }

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
