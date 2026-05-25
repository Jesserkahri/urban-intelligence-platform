package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.api.exception.ResourceNotFoundException;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.dto.DistrictCreateRequest;
import com.urban.intelligence.platform.dto.DistrictResponse;
import com.urban.intelligence.platform.dto.DistrictUpdateRequest;
import com.urban.intelligence.platform.dto.DistrictMetricsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DistrictService - Business logic for district management
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final IncidentRepository incidentRepository;

    /**
     * Create a new district
     */
    public DistrictResponse createDistrict(DistrictCreateRequest request) {
        log.info("Creating new district: {}", request.getName());
        
        if (districtRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("District with name '" + request.getName() + "' already exists");
        }

        District district = District.builder()
            .name(request.getName())
            .population(request.getPopulation())
            .sustainabilityScore(request.getSustainabilityScore())
            .operationalRiskScore(request.getOperationalRiskScore())
            .build();

        District savedDistrict = districtRepository.save(district);
        log.info("District created successfully with ID: {}", savedDistrict.getId());
        
        return mapToResponse(savedDistrict);
    }

    /**
     * Get district by ID
     */
    @Transactional(readOnly = true)
    public DistrictResponse getDistrictById(Long id) {
        District district = districtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + id));
        return mapToResponse(district);
    }

    /**
     * Get all districts with pagination
     */
    @Transactional(readOnly = true)
    public Page<DistrictResponse> getAllDistricts(Pageable pageable) {
        log.debug("Fetching all districts with pagination");
        return districtRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Update district
     */
    public DistrictResponse updateDistrict(Long id, DistrictUpdateRequest request) {
        log.info("Updating district with ID: {}", id);
        
        District district = districtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + id));

        if (request.getName() != null && !request.getName().equals(district.getName())) {
            if (districtRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException("District with name '" + request.getName() + "' already exists");
            }
            district.setName(request.getName());
        }
        if (request.getPopulation() != null) district.setPopulation(request.getPopulation());
        if (request.getSustainabilityScore() != null) district.setSustainabilityScore(request.getSustainabilityScore());
        if (request.getOperationalRiskScore() != null) district.setOperationalRiskScore(request.getOperationalRiskScore());

        District updatedDistrict = districtRepository.save(district);
        log.info("District updated successfully");
        
        return mapToResponse(updatedDistrict);
    }

    /**
     * Delete district
     */
    public void deleteDistrict(Long id) {
        log.info("Deleting district with ID: {}", id);
        
        if (!districtRepository.existsById(id)) {
            throw new ResourceNotFoundException("District not found with ID: " + id);
        }
        
        districtRepository.deleteById(id);
        log.info("District deleted successfully");
    }

    /**
     * Get districts ranked by risk
     */
    @Transactional(readOnly = true)
    public List<DistrictResponse> getDistrictsByHighestRisk() {
        log.debug("Fetching districts by highest risk");
        return districtRepository.findByHighestRisk().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get districts below sustainability threshold
     */
    @Transactional(readOnly = true)
    public List<DistrictResponse> getDistrictsBelowSustainabilityThreshold(Double threshold) {
        log.debug("Fetching districts below sustainability threshold: {}", threshold);
        return districtRepository.findBelowSustainabilityThreshold(threshold).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get district metrics with health score calculation
     */
    @Transactional(readOnly = true)
    public DistrictMetricsResponse getDistrictMetrics(Long id) {
        District district = districtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + id));
        
        // Health score calculation: (sustainability + (100 - risk)) / 2
        Double healthScore = (district.getSustainabilityScore() + (100.0 - district.getOperationalRiskScore())) / 2.0;
        
        Long recentIncidentsCount = incidentRepository.countIncidentsByDistrictAndDate(
            id, LocalDateTime.now().minusDays(7));

        return DistrictMetricsResponse.builder()
            .id(district.getId())
            .name(district.getName())
            .sustainabilityScore(district.getSustainabilityScore())
            .operationalRiskScore(district.getOperationalRiskScore())
            .healthScore(healthScore)
            .recentIncidentsCount(recentIncidentsCount.intValue())
            .build();
    }

    /**
     * Helper method to convert District to DistrictResponse DTO
     */
    private DistrictResponse mapToResponse(District district) {
        Long incidentCount = incidentRepository.countByDistrict(district);
        
        return DistrictResponse.builder()
            .id(district.getId())
            .name(district.getName())
            .population(district.getPopulation())
            .sustainabilityScore(district.getSustainabilityScore())
            .operationalRiskScore(district.getOperationalRiskScore())
            .incidentCount(incidentCount.intValue())
            .recommendationCount(district.getRecommendations().size())
            .build();
    }
}
