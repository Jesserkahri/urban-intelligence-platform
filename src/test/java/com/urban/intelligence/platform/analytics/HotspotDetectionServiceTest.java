package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.dto.analytics.HotspotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Analytics Service Unit Tests")
class HotspotDetectionServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private DistrictRepository districtRepository;

    @InjectMocks
    private HotspotDetectionService hotspotDetectionService;

    private District testDistrict;
    private List<Incident> testIncidents;

    @BeforeEach
    void setUp() {
        testDistrict = District.builder()
            .id(1L)
            .name("Test District")
            .population(50000)
            .sustainabilityScore(70.0)
            .operationalRiskScore(50.0)
            .build();

        testIncidents = createTestIncidents(testDistrict);
    }

    private List<Incident> createTestIncidents(District district) {
        return List.of(
            Incident.builder()
                .id(1L)
                .type("Traffic")
                .description("Heavy traffic")
                .severity(Incident.SeverityLevel.HIGH)
                .latitude(40.7128)
                .longitude(-74.0060)
                .district(district)
                .status(Incident.IncidentStatus.REPORTED)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build(),
            Incident.builder()
                .id(2L)
                .type("Traffic")
                .description("Accident")
                .severity(Incident.SeverityLevel.CRITICAL)
                .latitude(40.7129)
                .longitude(-74.0061)
                .district(district)
                .status(Incident.IncidentStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build(),
            Incident.builder()
                .id(3L)
                .type("Safety")
                .description("Fire alarm")
                .severity(Incident.SeverityLevel.MEDIUM)
                .latitude(40.7130)
                .longitude(-74.0062)
                .district(district)
                .status(Incident.IncidentStatus.RESOLVED)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build()
        );
    }

    @Test
    @DisplayName("Detect hotspot for district with incidents")
    void testDetectDistrictHotspotWithIncidents() {
        when(districtRepository.findById(1L)).thenReturn(Optional.of(testDistrict));
        when(incidentRepository.findByDistrictAndStatusIn(
            eq(testDistrict),
            argThat(list -> list.contains(Incident.IncidentStatus.REPORTED)))
            ).thenReturn(testIncidents.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.REPORTED || 
                           i.getStatus() == Incident.IncidentStatus.IN_PROGRESS)
                .toList());

        HotspotResponse response = hotspotDetectionService.detectDistrictHotspot(1L);

        assertNotNull(response);
        assertEquals("Test District", response.getDistrictName());
        assertEquals(2, response.getUnresolvedIncidentCount());
        assertTrue(response.getHotspotScore() > 0);
    }

    @Test
    @DisplayName("Detect hotspot throws exception for non-existent district")
    void testDetectDistrictHotspotDistrictNotFound() {
        when(districtRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            hotspotDetectionService.detectDistrictHotspot(999L);
        });
    }

    @Test
    @DisplayName("Hotspot score increases with critical incidents")
    void testHotspotScoreWithCriticalIncidents() {
        District criticalDistrict = District.builder()
            .id(2L)
            .name("Critical District")
            .population(50000)
            .sustainabilityScore(50.0)
            .operationalRiskScore(90.0)
            .build();

        List<Incident> criticalIncidents = List.of(
            Incident.builder()
                .id(101L)
                .type("Emergency")
                .description("Critical incident")
                .severity(Incident.SeverityLevel.CRITICAL)
                .latitude(40.7140)
                .longitude(-74.0070)
                .district(criticalDistrict)
                .status(Incident.IncidentStatus.REPORTED)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build(),
            Incident.builder()
                .id(102L)
                .type("Emergency")
                .description("Another critical")
                .severity(Incident.SeverityLevel.CRITICAL)
                .latitude(40.7141)
                .longitude(-74.0071)
                .district(criticalDistrict)
                .status(Incident.IncidentStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .build()
        );

        when(districtRepository.findById(2L)).thenReturn(Optional.of(criticalDistrict));
        when(incidentRepository.findByDistrictAndStatusIn(
            eq(criticalDistrict),
            argThat(list -> list.contains(Incident.IncidentStatus.REPORTED)))
            ).thenReturn(criticalIncidents);

        HotspotResponse response = hotspotDetectionService.detectDistrictHotspot(2L);

        assertNotNull(response);
        assertEquals(2, response.getUnresolvedIncidentCount());
        assertTrue(response.getHotspotScore() > 5.0);
    }

    @Test
    @DisplayName("Hotspot detection ignores resolved incidents")
    void testHotspotIgnoresResolvedIncidents() {
        when(districtRepository.findById(1L)).thenReturn(Optional.of(testDistrict));
        List<Incident> unresolvedIncidents = testIncidents.stream()
            .filter(i -> i.getStatus() != Incident.IncidentStatus.RESOLVED)
            .toList();
        
        when(incidentRepository.findByDistrictAndStatusIn(
            eq(testDistrict),
            argThat(list -> list.contains(Incident.IncidentStatus.REPORTED)))
            ).thenReturn(unresolvedIncidents);

        HotspotResponse response = hotspotDetectionService.detectDistrictHotspot(1L);

        assertNotNull(response);
        assertEquals(2, response.getUnresolvedIncidentCount());
    }

    @Test
    @DisplayName("Average severity is calculated correctly")
    void testAverageSeverityCalculation() {
        when(districtRepository.findById(1L)).thenReturn(Optional.of(testDistrict));
        when(incidentRepository.findByDistrictAndStatusIn(
            eq(testDistrict),
            argThat(list -> list.contains(Incident.IncidentStatus.REPORTED)))
            ).thenReturn(testIncidents.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.REPORTED || 
                           i.getStatus() == Incident.IncidentStatus.IN_PROGRESS)
                .toList());

        HotspotResponse response = hotspotDetectionService.detectDistrictHotspot(1L);

        assertNotNull(response);
        // HIGH (4) + CRITICAL (7) / 2 = 5.5 → avgWeight >= 5 → "CRITICAL"
        assertEquals("CRITICAL", response.getAverageSeverity());
    }
}