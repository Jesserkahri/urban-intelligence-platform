package com.urban.intelligence.platform.repository;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for DistrictRepository.
 * Runs against PostgreSQL Testcontainers so custom queries are validated
 * against the same dialect as production.
 */
@DisplayName("DistrictRepository Tests")
class DistrictRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private DistrictRepository districtRepository;

    private District district1;
    private District district2;
    private District district3;

    @BeforeEach
    void setUp() {
        districtRepository.deleteAll();

        // Create test districts
        district1 = District.builder()
            .name("High Risk District")
            .population(50000)
            .sustainabilityScore(45.0)
            .operationalRiskScore(85.0)
            .build();

        district2 = District.builder()
            .name("Sustainable District")
            .population(30000)
            .sustainabilityScore(85.0)
            .operationalRiskScore(25.0)
            .build();

        district3 = District.builder()
            .name("Medium Risk District")
            .population(40000)
            .sustainabilityScore(65.0)
            .operationalRiskScore(55.0)
            .build();

        List<District> savedDistricts = districtRepository.saveAll(List.of(district1, district2, district3));
        district1 = savedDistricts.get(0);
        district2 = savedDistricts.get(1);
        district3 = savedDistricts.get(2);
    }

    @Test
    @DisplayName("Find district by name")
    void testFindByName() {
        // Act
        Optional<District> result = districtRepository.findByName("High Risk District");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("High Risk District", result.get().getName());
        assertEquals(85.0, result.get().getOperationalRiskScore());
    }

    @Test
    @DisplayName("Find by name returns empty when not found")
    void testFindByNameNotFound() {
        // Act
        Optional<District> result = districtRepository.findByName("Nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Find districts by highest risk")
    void testFindByHighestRisk() {
        // Act
        List<District> results = districtRepository.findByHighestRisk();

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        // Verify ordered by highest risk first
        assertEquals(85.0, results.get(0).getOperationalRiskScore());
        assertEquals(55.0, results.get(1).getOperationalRiskScore());
        assertEquals(25.0, results.get(2).getOperationalRiskScore());
    }

    @Test
    @DisplayName("Find districts below sustainability threshold")
    void testFindBelowSustainabilityThreshold() {
        // Act
        List<District> results = districtRepository.findBelowSustainabilityThreshold(70.0);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(d -> d.getSustainabilityScore() < 70.0));
    }

    @Test
    @DisplayName("Find districts above risk threshold")
    void testFindAboveRiskThreshold() {
        // Act
        List<District> results = districtRepository.findAboveRiskThreshold(50.0);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(d -> d.getOperationalRiskScore() > 50.0));
    }

    @Test
    @DisplayName("Get average operational risk score")
    void testGetAverageOperationalRiskScore() {
        // Act
        Double average = districtRepository.getAverageOperationalRiskScore();

        // Assert
        assertNotNull(average);
        // (85 + 25 + 55) / 3 = 55
        assertEquals(55.0, average, 0.1);
    }

    @Test
    @DisplayName("Get average sustainability score")
    void testGetAverageSustainabilityScore() {
        // Act
        Double average = districtRepository.getAverageSustainabilityScore();

        // Assert
        assertNotNull(average);
        // (45 + 85 + 65) / 3 = 65
        assertEquals(65.0, average, 0.1);
    }

    @Test
    @DisplayName("Get incident count for district")
    void testGetIncidentCount() {
        // Act
        Long count = districtRepository.getIncidentCount(district1);

        // Assert
        assertNotNull(count);
        // No incidents created in setUp, so should be 0
        assertEquals(0L, count);
    }
}
