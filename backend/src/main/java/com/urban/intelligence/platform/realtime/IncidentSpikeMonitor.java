package com.urban.intelligence.platform.realtime;

import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentSpikeMonitor {

    private static final int SPIKE_THRESHOLD = 5;
    private static final int CRITICAL_SPIKE_THRESHOLD = 2;
    private static final int WINDOW_MINUTES = 15;

    private final IncidentRepository incidentRepository;
    private final RealTimeOperationsService realTimeOperationsService;
    private final Map<String, LocalDateTime> lastAlertByKey = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${app.realtime.spike-check-ms:60000}")
    @Transactional(readOnly = true)
    public void detectIncidentSpikes() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(WINDOW_MINUTES);
        List<Incident> recent = incidentRepository.findByCreatedAtAfter(since);
        if (recent.size() >= SPIKE_THRESHOLD) {
            notifyOnce("GLOBAL", "INCIDENT_SPIKE", "HIGH", "Incident spike detected",
                    recent.size() + " incidents reported in the last " + WINDOW_MINUTES + " minutes", null);
        }

        long critical = recent.stream().filter(incident -> incident.getSeverity() == Incident.SeverityLevel.CRITICAL).count();
        if (critical >= CRITICAL_SPIKE_THRESHOLD) {
            notifyOnce("CRITICAL", "CRITICAL_SPIKE", "CRITICAL", "Critical incident spike",
                    critical + " critical incidents reported in the last " + WINDOW_MINUTES + " minutes", null);
        }

        Map<Long, List<Incident>> byDistrict = recent.stream()
                .filter(incident -> incident.getDistrict() != null)
                .collect(Collectors.groupingBy(incident -> incident.getDistrict().getId()));
        byDistrict.forEach((districtId, incidents) -> {
            if (incidents.size() >= 3) {
                notifyOnce("DISTRICT_" + districtId, "DISTRICT_SPIKE", "HIGH", "District incident spike",
                        incidents.size() + " incidents reported in one district in the last " + WINDOW_MINUTES + " minutes", districtId);
            }
        });
    }

    private void notifyOnce(String key, String type, String severity, String title, String message, Long districtId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previous = lastAlertByKey.get(key);
        if (previous != null && previous.isAfter(now.minusMinutes(WINDOW_MINUTES))) {
            return;
        }
        lastAlertByKey.put(key, now);
        realTimeOperationsService.createAlertNotification(type, severity, title, message, null, districtId);
        log.warn("{}: {}", title, message);
    }
}
