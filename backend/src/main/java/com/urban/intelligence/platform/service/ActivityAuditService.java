package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.domain.entity.ActivityEvent;
import com.urban.intelligence.platform.domain.repository.ActivityEventRepository;
import com.urban.intelligence.platform.dto.ActivityEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityAuditService {

    private final ActivityEventRepository activityEventRepository;

    @Transactional
    public void record(String entityType, Long entityId, String action, String actor, String details) {
        activityEventRepository.save(ActivityEvent.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action(action)
            .actor(actor)
            .details(details)
            .build());
    }

    @Transactional(readOnly = true)
    public List<ActivityEventResponse> timeline(String entityType, Long entityId) {
        return activityEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private ActivityEventResponse mapToResponse(ActivityEvent event) {
        return ActivityEventResponse.builder()
            .id(event.getId())
            .entityType(event.getEntityType())
            .entityId(event.getEntityId())
            .action(event.getAction())
            .actor(event.getActor())
            .details(event.getDetails())
            .createdAt(event.getCreatedAt())
            .build();
    }
}
