package com.urban.intelligence.platform.config;

import com.urban.intelligence.platform.analytics.OperationalInsightService;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * RecommendationInitializer - Auto-generates recommendations on application startup.
 *
 * Triggered after the application context is fully initialized.
 * Generates recommendations for all districts with incident data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationInitializer {

    private final OperationalInsightService operationalInsightService;
    private final DistrictRepository districtRepository;
    private final RecommendationRepository recommendationRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRecommendations() {
        log.info("INIT: starting auto-generation of recommendations for all districts");
        
        try {
            long existingRecommendations = recommendationRepository.count();
            if (existingRecommendations > 0) {
                log.info("INIT: {} recommendations already exist, skipping initialization", existingRecommendations);
                return;
            }

            districtRepository.findAll().forEach(district -> {
                try {
                    operationalInsightService.generateDistrictRecommendations(district.getId());
                    log.debug("INIT: generated recommendations for district: {}", district.getId());
                } catch (Exception e) {
                    log.warn("INIT: failed to generate recommendations for district {}: {}", 
                        district.getId(), e.getMessage());
                }
            });
            
            long generatedCount = recommendationRepository.count();
            log.info("INIT: completed recommendation initialization - {} total recommendations in database", generatedCount);
        } catch (Exception e) {
            log.error("INIT: error during recommendation initialization", e);
        }
    }
}
