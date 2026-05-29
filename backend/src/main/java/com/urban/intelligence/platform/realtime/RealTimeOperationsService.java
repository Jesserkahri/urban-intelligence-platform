package com.urban.intelligence.platform.realtime;

import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.entity.OperationalNotification;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.domain.repository.OperationalNotificationRepository;
import com.urban.intelligence.platform.dto.IncidentResponse;
import com.urban.intelligence.platform.dto.LiveDashboardSnapshotResponse;
import com.urban.intelligence.platform.dto.LiveOperationEvent;
import com.urban.intelligence.platform.dto.OperationalNotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeOperationsService {

    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;

    private final OperationalNotificationRepository notificationRepository;
    private final IncidentRepository incidentRepository;
    private final List<ClientSubscription> subscriptions = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(String channel) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        String normalizedChannel = normalizeChannel(channel);
        ClientSubscription subscription = new ClientSubscription(emitter, normalizedChannel);
        subscriptions.add(subscription);

        emitter.onCompletion(() -> subscriptions.remove(subscription));
        emitter.onTimeout(() -> subscriptions.remove(subscription));
        emitter.onError(error -> subscriptions.remove(subscription));

        send(subscription, LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type("STREAM_CONNECTED")
                .channel(normalizedChannel)
                .severity("INFO")
                .title("Live operations connected")
                .message("Subscribed to " + normalizedChannel + " event stream")
                .occurredAt(LocalDateTime.now())
                .payload(Map.of("retryMillis", 5000))
                .build());
        return emitter;
    }

    @Transactional
    public void publishIncidentCreated(Incident incident, IncidentResponse response) {
        publish(LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type("INCIDENT_CREATED")
                .channel("incidents")
                .severity(response.getSeverity())
                .title("New " + response.getSeverity().toLowerCase() + " incident")
                .message(response.getType() + " reported in " + response.getDistrictName())
                .incidentId(response.getId())
                .districtId(response.getDistrictId())
                .occurredAt(LocalDateTime.now())
                .payload(Map.of("incident", response))
                .build());

        if (incident.getSeverity() == Incident.SeverityLevel.CRITICAL) {
            createAlertNotification(
                    "CRITICAL_INCIDENT",
                    "CRITICAL",
                    "Critical incident reported",
                    response.getType() + " requires immediate attention in " + response.getDistrictName(),
                    response.getId(),
                    response.getDistrictId());
        }
        publishDashboardSnapshot();
    }

    @Transactional
    public void publishIncidentUpdated(Incident previous, Incident updated, IncidentResponse response) {
        String eventType = previous.getStatus() != updated.getStatus() ? "INCIDENT_STATUS_CHANGED" : "INCIDENT_UPDATED";
        publish(LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(eventType)
                .channel("incidents")
                .severity(response.getSeverity())
                .title(eventType.equals("INCIDENT_STATUS_CHANGED") ? "Incident status changed" : "Incident updated")
                .message(response.getType() + " is now " + response.getStatus())
                .incidentId(response.getId())
                .districtId(response.getDistrictId())
                .occurredAt(LocalDateTime.now())
                .payload(Map.of(
                        "incident", response,
                        "previousStatus", previous.getStatus().name(),
                        "currentStatus", updated.getStatus().name()))
                .build());

        if (updated.getSeverity() == Incident.SeverityLevel.CRITICAL
                && updated.getStatus() != Incident.IncidentStatus.RESOLVED
                && updated.getStatus() != Incident.IncidentStatus.CLOSED) {
            createAlertNotification(
                    "ESCALATION",
                    "CRITICAL",
                    "Critical incident escalation",
                    response.getType() + " remains active with status " + response.getStatus(),
                    response.getId(),
                    response.getDistrictId());
        }
        publishDashboardSnapshot();
    }

    @Transactional
    public void publishIncidentDeleted(Long incidentId) {
        publish(LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type("INCIDENT_DELETED")
                .channel("incidents")
                .severity("INFO")
                .title("Incident deleted")
                .message("Incident " + incidentId + " was removed")
                .incidentId(incidentId)
                .occurredAt(LocalDateTime.now())
                .payload(Map.of("incidentId", incidentId))
                .build());
        publishDashboardSnapshot();
    }

    @Transactional
    public void createAlertNotification(String type, String severity, String title, String message, Long incidentId, Long districtId) {
        OperationalNotification notification = notificationRepository.save(OperationalNotification.builder()
                .type(type)
                .severity(severity)
                .title(title)
                .message(message)
                .incidentId(incidentId)
                .districtId(districtId)
                .acknowledged(false)
                .build());

        LiveOperationEvent event = LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .channel("alerts")
                .severity(severity)
                .title(title)
                .message(message)
                .incidentId(incidentId)
                .districtId(districtId)
                .occurredAt(notification.getCreatedAt())
                .payload(Map.of("notification", mapNotification(notification)))
                .build();
        publish(event);
        publish(LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type("NOTIFICATION_CREATED")
                .channel("notifications")
                .severity(severity)
                .title(title)
                .message(message)
                .incidentId(incidentId)
                .districtId(districtId)
                .occurredAt(notification.getCreatedAt())
                .payload(Map.of("notification", mapNotification(notification)))
                .build());
    }

    @Transactional(readOnly = true)
    public Page<OperationalNotificationResponse> getNotifications(Pageable pageable, Boolean unreadOnly, String severity) {
        Page<OperationalNotification> page;
        if (Boolean.TRUE.equals(unreadOnly)) {
            page = notificationRepository.findByAcknowledgedFalse(pageable);
        } else if (severity != null && !severity.isBlank()) {
            page = notificationRepository.findBySeverity(severity.toUpperCase(), pageable);
        } else {
            page = notificationRepository.findAll(pageable);
        }
        return page.map(this::mapNotification);
    }

    @Transactional
    public OperationalNotificationResponse acknowledgeNotification(Long id) {
        OperationalNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        notification.setAcknowledged(true);
        notification.setReadAt(LocalDateTime.now());
        OperationalNotification saved = notificationRepository.save(notification);
        OperationalNotificationResponse response = mapNotification(saved);
        publish(LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type("NOTIFICATION_ACKNOWLEDGED")
                .channel("notifications")
                .severity(saved.getSeverity())
                .title("Notification acknowledged")
                .message(saved.getTitle())
                .incidentId(saved.getIncidentId())
                .districtId(saved.getDistrictId())
                .occurredAt(LocalDateTime.now())
                .payload(Map.of("notification", response))
                .build());
        publishDashboardSnapshot();
        return response;
    }

    @Transactional(readOnly = true)
    public LiveDashboardSnapshotResponse getDashboardSnapshot() {
        LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
        List<Incident> incidents = incidentRepository.findAll();
        Map<String, Long> statusCounters = incidents.stream()
                .collect(Collectors.groupingBy(incident -> incident.getStatus().name(), Collectors.counting()));
        Map<String, Long> severityCounters = incidents.stream()
                .collect(Collectors.groupingBy(incident -> incident.getSeverity().name(), Collectors.counting()));
        long unresolved = incidents.stream().filter(incident -> incident.getStatus() == Incident.IncidentStatus.REPORTED
                || incident.getStatus() == Incident.IncidentStatus.OPEN
                || incident.getStatus() == Incident.IncidentStatus.IN_PROGRESS).count();
        long active = incidents.stream().filter(incident -> incident.getStatus() != Incident.IncidentStatus.CLOSED
                && incident.getStatus() != Incident.IncidentStatus.RESOLVED).count();

        return LiveDashboardSnapshotResponse.builder()
                .activeIncidents(active)
                .criticalIncidents24h(incidents.stream().filter(incident -> incident.getCreatedAt().isAfter(dayAgo)
                        && incident.getSeverity() == Incident.SeverityLevel.CRITICAL).count())
                .unresolvedIncidents(unresolved)
                .unreadNotifications(notificationRepository.countByAcknowledgedFalse())
                .alerts24h(notificationRepository.countBySeverityAndCreatedAtAfter("CRITICAL", dayAgo))
                .statusCounters(statusCounters)
                .severityCounters(severityCounters)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public void publishDashboardSnapshot() {
        LiveDashboardSnapshotResponse snapshot = getDashboardSnapshot();
        publish(LiveOperationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type("DASHBOARD_SNAPSHOT")
                .channel("dashboard")
                .severity("INFO")
                .title("Dashboard updated")
                .message("Operational counters refreshed")
                .occurredAt(LocalDateTime.now())
                .payload(Map.of("snapshot", snapshot))
                .build());
    }

    public void publish(LiveOperationEvent event) {
        subscriptions.stream()
                .filter(matches(event))
                .forEach(subscription -> send(subscription, event));
    }

    private Predicate<ClientSubscription> matches(LiveOperationEvent event) {
        return subscription -> "all".equals(subscription.channel()) || subscription.channel().equals(event.getChannel());
    }

    private void send(ClientSubscription subscription, LiveOperationEvent event) {
        try {
            subscription.emitter().send(SseEmitter.event()
                    .id(event.getId())
                    .name(event.getType())
                    .reconnectTime(5000)
                    .data(event));
        } catch (IOException | IllegalStateException e) {
            subscriptions.remove(subscription);
        }
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) return "all";
        return channel.toLowerCase();
    }

    private OperationalNotificationResponse mapNotification(OperationalNotification notification) {
        return OperationalNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .severity(notification.getSeverity())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .incidentId(notification.getIncidentId())
                .districtId(notification.getDistrictId())
                .acknowledged(notification.getAcknowledged())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private record ClientSubscription(SseEmitter emitter, String channel) {
    }
}
