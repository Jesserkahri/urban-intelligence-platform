package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.domain.repository.RecommendationRepository;
import com.urban.intelligence.platform.dto.intelligence.AnomalyResponse;
import com.urban.intelligence.platform.dto.intelligence.ForecastResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntelligenceMaturityServiceTest {

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private DistrictRiskScoringService riskScoringService;

    private IntelligenceMaturityService service;
    private District district;

    @BeforeEach
    void setUp() {
        service = new IntelligenceMaturityService(
                districtRepository,
                incidentRepository,
                recommendationRepository,
                riskScoringService);
        district = District.builder()
                .id(1L)
                .name("Central")
                .population(10000)
                .sustainabilityScore(72.0)
                .operationalRiskScore(48.0)
                .build();
    }

    @Test
    @DisplayName("Rolling baseline anomaly flags a 2-standard-deviation spike")
    void detectsRollingBaselineSpike() {
        when(districtRepository.findAll()).thenReturn(List.of(district));
        when(incidentRepository.getDailyIncidentCountsByDistrict(any()))
                .thenReturn(districtRowsWithFinalSpike());

        List<AnomalyResponse> anomalies = service.detectIncidentAnomalies();

        assertFalse(anomalies.isEmpty());
        AnomalyResponse anomaly = anomalies.get(0);
        assertEquals(1L, anomaly.getDistrictId());
        assertEquals("ABOVE_BASELINE", anomaly.getDirection());
        assertTrue(anomaly.getAnomalyScore() >= 2.0);
        assertTrue(anomaly.getConfidence() >= 50.0);
    }

    @Test
    @DisplayName("Forecast returns 7 explainable points with confidence intervals")
    void forecastsWithWeightedMovingAverageAndWeekdayPattern() {
        when(districtRepository.findById(1L)).thenReturn(Optional.of(district));
        when(incidentRepository.getDailyIncidentCountsForDistrict(eq(1L), any()))
                .thenReturn(districtRowsWithFinalSpike().stream()
                        .map(row -> new Object[]{row[1], row[2]})
                        .toList());

        ForecastResponse forecast = service.forecastDistrict(1L, 7);

        assertEquals(7, forecast.getForecast().size());
        assertTrue(forecast.getPredictedIncidents() > 0);
        assertTrue(forecast.getConfidence() > 0);
        assertTrue(forecast.getExplanation().contains("weighted 14-day moving average"));
        assertTrue(forecast.getForecast().stream()
                .allMatch(point -> point.getUpperBound() >= point.getLowerBound()));
    }

    private List<Object[]> districtRowsWithFinalSpike() {
        List<Object[]> rows = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(89);
        for (int i = 0; i < 89; i++) {
            rows.add(new Object[]{1L, Date.valueOf(start.plusDays(i)), 2L});
        }
        rows.add(new Object[]{1L, Date.valueOf(LocalDate.now()), 12L});
        return rows;
    }
}
