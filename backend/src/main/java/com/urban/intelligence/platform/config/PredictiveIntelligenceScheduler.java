package com.urban.intelligence.platform.config;

import com.urban.intelligence.platform.analytics.PredictiveIntelligenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PredictiveIntelligenceScheduler {

    private final PredictiveIntelligenceService predictiveIntelligenceService;

    @Scheduled(cron = "${app.predictive.alert-cron:0 10 * * * ?}")
    public void generatePredictiveWarnings() {
        try {
            int generated = predictiveIntelligenceService.generatePredictiveAlerts(7).size();
            if (generated > 0) {
                log.warn("PREDICTIVE: generated {} early warning alerts", generated);
            } else {
                log.debug("PREDICTIVE: no early warnings generated");
            }
        } catch (Exception e) {
            log.warn("PREDICTIVE: alert generation failed: {}", e.getMessage());
        }
    }
}
