package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.api.exception.ResourceNotFoundException;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.dto.IncidentCreateRequest;
import com.urban.intelligence.platform.dto.IncidentResponse;
import com.urban.intelligence.platform.dto.IncidentUpdateRequest;
import com.urban.intelligence.platform.realtime.RealTimeOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final DistrictRepository districtRepository;
    private final RealTimeOperationsService realTimeOperationsService;
    private final ActivityAuditService activityAuditService;

    public IncidentResponse createIncident(IncidentCreateRequest request) {
        log.info("Creating new incident of type: {}", request.getType());

        District district = districtRepository.findById(request.getDistrictId())
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + request.getDistrictId()));

        Incident incident = Incident.builder()
            .type(request.getType())
            .description(request.getDescription())
            .severity(Incident.SeverityLevel.valueOf(request.getSeverity().toUpperCase()))
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .district(district)
            .status(Incident.IncidentStatus.OPEN)
            .build();

        Incident savedIncident = incidentRepository.save(incident);
        IncidentResponse response = mapToResponse(savedIncident);
        activityAuditService.record("INCIDENT", savedIncident.getId(), "CREATED", "system",
            "Incident created in " + district.getName());
        realTimeOperationsService.publishIncidentCreated(savedIncident, response);
        log.info("Incident created successfully with ID: {}", savedIncident.getId());
        return response;
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        return mapToResponse(incident);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getAllIncidents(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidentsByDistrict(Long districtId, Pageable pageable) {
        if (!districtRepository.existsById(districtId)) {
            throw new ResourceNotFoundException("District not found with ID: " + districtId);
        }
        return incidentRepository.findByDistrict_Id(districtId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getIncidentsByStatus(String status, Pageable pageable) {
        Incident.IncidentStatus incidentStatus = Incident.IncidentStatus.valueOf(status.toUpperCase());
        return incidentRepository.findByStatus(incidentStatus, pageable).map(this::mapToResponse);
    }

    public IncidentResponse updateIncident(Long id, IncidentUpdateRequest request) {
        log.info("Updating incident with ID: {}", id);

        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        Incident previous = Incident.builder()
            .status(incident.getStatus())
            .severity(incident.getSeverity())
            .build();

        if (request.getType() != null) incident.setType(request.getType());
        if (request.getDescription() != null) incident.setDescription(request.getDescription());
        if (request.getSeverity() != null) incident.setSeverity(Incident.SeverityLevel.valueOf(request.getSeverity().toUpperCase()));
        if (request.getLatitude() != null) incident.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) incident.setLongitude(request.getLongitude());
        if (request.getStatus() != null) incident.setStatus(Incident.IncidentStatus.valueOf(request.getStatus().toUpperCase()));
        if (request.getAssignedTo() != null) incident.setAssignedTo(request.getAssignedTo().trim().isBlank() ? null : request.getAssignedTo().trim());
        if (request.getAcknowledged() != null) {
            incident.setAcknowledged(request.getAcknowledged());
            incident.setAcknowledgedAt(request.getAcknowledged() ? LocalDateTime.now() : null);
        }
        if (request.getReviewed() != null) {
            incident.setReviewed(request.getReviewed());
            incident.setReviewedAt(request.getReviewed() ? LocalDateTime.now() : null);
        }
        if (request.getReviewNotes() != null) incident.setReviewNotes(request.getReviewNotes());

        Incident updatedIncident = incidentRepository.save(incident);
        IncidentResponse response = mapToResponse(updatedIncident);
        activityAuditService.record("INCIDENT", updatedIncident.getId(), "UPDATED", "system", "Incident updated");
        realTimeOperationsService.publishIncidentUpdated(previous, updatedIncident, response);
        log.info("Incident updated successfully");
        return response;
    }

    public void deleteIncident(Long id) {
        log.info("Deleting incident with ID: {}", id);

        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Incident not found with ID: " + id);
        }

        incidentRepository.deleteById(id);
        activityAuditService.record("INCIDENT", id, "DELETED", "system", "Incident deleted");
        realTimeOperationsService.publishIncidentDeleted(id);
        log.info("Incident deleted successfully");
    }

    public IncidentResponse assignIncident(Long id, String assignedTo, String actor) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        incident.setAssignedTo(assignedTo.trim());
        if (incident.getStatus() == Incident.IncidentStatus.REPORTED || incident.getStatus() == Incident.IncidentStatus.OPEN) {
            incident.setStatus(Incident.IncidentStatus.IN_PROGRESS);
        }
        Incident saved = incidentRepository.save(incident);
        IncidentResponse response = mapToResponse(saved);
        activityAuditService.record("INCIDENT", id, "ASSIGNED", actor, "Assigned to " + assignedTo.trim());
        realTimeOperationsService.publishIncidentUpdated(Incident.builder().status(incident.getStatus()).severity(incident.getSeverity()).build(), saved, response);
        return response;
    }

    public IncidentResponse acknowledgeIncident(Long id, String actor) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        incident.setAcknowledged(true);
        incident.setAcknowledgedAt(LocalDateTime.now());
        Incident saved = incidentRepository.save(incident);
        IncidentResponse response = mapToResponse(saved);
        activityAuditService.record("INCIDENT", id, "ACKNOWLEDGED", actor, "Incident acknowledged");
        realTimeOperationsService.publishIncidentUpdated(Incident.builder().status(incident.getStatus()).severity(incident.getSeverity()).build(), saved, response);
        return response;
    }

    public IncidentResponse reviewIncident(Long id, String notes, String actor) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        incident.setReviewed(true);
        incident.setReviewedAt(LocalDateTime.now());
        incident.setReviewNotes(notes);
        Incident saved = incidentRepository.save(incident);
        IncidentResponse response = mapToResponse(saved);
        activityAuditService.record("INCIDENT", id, "REVIEWED", actor, notes == null || notes.isBlank() ? "Incident reviewed" : notes);
        realTimeOperationsService.publishIncidentUpdated(Incident.builder().status(incident.getStatus()).severity(incident.getSeverity()).build(), saved, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getActiveIncidentsByDistrict(Long districtId) {
        return incidentRepository.findActiveIncidentsByDistrict(districtId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getIncidentsByGeoFilter(
            Double minLat,
            Double maxLat,
            Double minLon,
            Double maxLon,
            String severity,
            Long districtId) {
        List<Incident> incidents;
        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            incidents = incidentRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon);
        } else {
            incidents = incidentRepository.findAll();
        }

        if (districtId != null) {
            incidents = incidents.stream()
                .filter(incident -> incident.getDistrict().getId().equals(districtId))
                .collect(Collectors.toList());
        }

        if (severity != null && !severity.isBlank()) {
            incidents = incidents.stream()
                .filter(incident -> incident.getSeverity().toString().equalsIgnoreCase(severity))
                .collect(Collectors.toList());
        }

        return incidents.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getRecentIncidents() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return incidentRepository.findByCreatedAtAfter(sevenDaysAgo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private IncidentResponse mapToResponse(Incident incident) {
        return IncidentResponse.builder()
            .id(incident.getId())
            .title(incident.getType())
            .type(incident.getType())
            .description(incident.getDescription())
            .severity(incident.getSeverity().toString())
            .latitude(incident.getLatitude())
            .longitude(incident.getLongitude())
            .districtId(incident.getDistrict().getId())
            .districtName(incident.getDistrict().getName())
            .status(incident.getStatus().toString())
            .assignedTo(incident.getAssignedTo())
            .acknowledged(Boolean.TRUE.equals(incident.getAcknowledged()))
            .acknowledgedAt(incident.getAcknowledgedAt())
            .reviewed(Boolean.TRUE.equals(incident.getReviewed()))
            .reviewedAt(incident.getReviewedAt())
            .reviewNotes(incident.getReviewNotes())
            .createdAt(incident.getCreatedAt())
            .updatedAt(incident.getUpdatedAt())
            .build();
    }
}
