package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.api.exception.ResourceNotFoundException;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.dto.IncidentCreateRequest;
import com.urban.intelligence.platform.dto.IncidentResponse;
import com.urban.intelligence.platform.dto.IncidentUpdateRequest;
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
 * IncidentService - Business logic for incident management
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final DistrictRepository districtRepository;

    /**
     * Create a new incident
     */
    public IncidentResponse createIncident(IncidentCreateRequest request) {
        log.info("Creating new incident of type: {}", request.getType());
        
        District district = districtRepository.findById(request.getDistrictId())
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + request.getDistrictId()));

        Incident incident = Incident.builder()
            .type(request.getType())
            .description(request.getDescription())
            .severity(Incident.SeverityLevel.valueOf(request.getSeverity()))
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .district(district)
            .status(Incident.IncidentStatus.REPORTED)
            .build();

        Incident savedIncident = incidentRepository.save(incident);
        log.info("Incident created successfully with ID: {}", savedIncident.getId());
        
        return mapToResponse(savedIncident);
    }

    /**
     * Get incident by ID
     */
    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        return mapToResponse(incident);
    }

    /**
     * Get all incidents with pagination
     */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
        log.debug("Fetching all incidents with pagination");
        return incidentRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get incidents by district
     */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidentsByDistrict(Long districtId, Pageable pageable) {
        log.debug("Fetching incidents for district: {}", districtId);
        
        if (!districtRepository.existsById(districtId)) {
            throw new ResourceNotFoundException("District not found with ID: " + districtId);
        }
        
        return incidentRepository.findByDistrict_Id(districtId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get incidents by status
     */
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidentsByStatus(String status, Pageable pageable) {
        log.debug("Fetching incidents by status: {}", status);
        Incident.IncidentStatus incidentStatus = Incident.IncidentStatus.valueOf(status.toUpperCase());
        return incidentRepository.findByStatus(incidentStatus, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Update incident
     */
    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request) {
        log.info("Updating incident with ID: {}", id);
        
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        if (request.getType() != null) incident.setType(request.getType());
        if (request.getDescription() != null) incident.setDescription(request.getDescription());
        if (request.getSeverity() != null) incident.setSeverity(Incident.SeverityLevel.valueOf(request.getSeverity()));
        if (request.getLatitude() != null) incident.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) incident.setLongitude(request.getLongitude());
        if (request.getStatus() != null) incident.setStatus(Incident.IncidentStatus.valueOf(request.getStatus()));

        Incident updatedIncident = incidentRepository.save(incident);
        log.info("Incident updated successfully");
        
        return mapToResponse(updatedIncident);
    }

    /**
     * Delete incident
     */
    public void deleteIncident(Long id) {
        log.info("Deleting incident with ID: {}", id);
        
        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Incident not found with ID: " + id);
        }
        
        incidentRepository.deleteById(id);
        log.info("Incident deleted successfully");
    }

    /**
     * Get active incidents for a district
     */
    @Transactional(readOnly = true)
    public List<IncidentResponse> getActiveIncidentsByDistrict(Long districtId) {
        log.debug("Fetching active incidents for district: {}", districtId);
        return incidentRepository.findActiveIncidentsByDistrict(districtId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get recent incidents (within last 7 days)
     */
    @Transactional(readOnly = true)
    public List<IncidentResponse> getRecentIncidents() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return incidentRepository.findByCreatedAtAfter(sevenDaysAgo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Helper method to convert Incident to IncidentResponse DTO
     */
    private IncidentResponse mapToResponse(Incident incident) {
        return IncidentResponse.builder()
            .id(incident.getId())
            .type(incident.getType())
            .description(incident.getDescription())
            .severity(incident.getSeverity().toString())
            .latitude(incident.getLatitude())
            .longitude(incident.getLongitude())
            .districtId(incident.getDistrict().getId())
            .status(incident.getStatus().toString())
            .createdAt(incident.getCreatedAt())
            .updatedAt(incident.getUpdatedAt())
            .build();
    }
}
