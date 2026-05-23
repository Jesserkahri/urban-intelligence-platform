package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.dto.DistrictCreateRequest;
import com.urban.intelligence.platform.dto.DistrictResponse;
import com.urban.intelligence.platform.dto.DistrictUpdateRequest;
import com.urban.intelligence.platform.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DistrictService
 * Tests CRUD operations, filtering, and analytics methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DistrictService Unit Tests")
class DistrictServiceTest {

    @Mock
    private DistrictRepository districtRepository;

    @InjectMocks
    private DistrictService districtService;

    private District testDistrict;
    private DistrictCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testDistrict = District.builder()
            .id(1L)
            .name("Downtown District")
            .population(50000)
            .sustainabilityScore(75.5)
            .operationalRiskScore(35.2)
            .build();

        createRequest = DistrictCreateRequest.builder()
            .name("New District")
            .population(25000)
            .sustainabilityScore(65.0)
            .operationalRiskScore(40.0)
            .build();
    }

    @Test
    @DisplayName("Create district successfully")
    void testCreateDistrictSuccess() {
        // Arrange
        when(districtRepository.save(any(District.class))).thenReturn(testDistrict);

        // Act
        DistrictResponse response = districtService.createDistrict(createRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Downtown District", response.getName());
        assertEquals(50000, response.getPopulation());
        verify(districtRepository).save(any(District.class));
    }

    @Test
    @DisplayName("Get district by ID successfully")
    void testGetDistrictByIdSuccess() {
        // Arrange
        when(districtRepository.findById(1L)).thenReturn(Optional.of(testDistrict));

        // Act
        DistrictResponse response = districtService.getDistrictById(1L);

        // Assert
        assertNotNull(response);
        assertEquals("Downtown District", response.getName());
    }

    @Test
    @DisplayName("Get district by ID throws exception when not found")
    void testGetDistrictByIdNotFound() {
        // Arrange
        when(districtRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            districtService.getDistrictById(999L);
        });
    }

    @Test
    @DisplayName("Get all districts with pagination")
    void testGetAllDistrictsPaginated() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<District> districtPage = new PageImpl<>(List.of(testDistrict));
        when(districtRepository.findAll(pageable)).thenReturn(districtPage);

        // Act
        Page<DistrictResponse> response = districtService.getAllDistricts(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Downtown District", response.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Update district successfully")
    void testUpdateDistrictSuccess() {
        // Arrange
        DistrictUpdateRequest updateRequest = DistrictUpdateRequest.builder()
            .population(60000)
            .sustainabilityScore(80.0)
            .operationalRiskScore(30.0)
            .build();

        District updatedDistrict = District.builder()
            .id(1L)
            .name("Downtown District")
            .population(60000)
            .sustainabilityScore(80.0)
            .operationalRiskScore(30.0)
            .build();

        when(districtRepository.findById(1L)).thenReturn(Optional.of(testDistrict));
        when(districtRepository.save(any(District.class))).thenReturn(updatedDistrict);

        // Act
        DistrictResponse response = districtService.updateDistrict(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(60000, response.getPopulation());
        assertEquals(80.0, response.getSustainabilityScore());
    }

    @Test
    @DisplayName("Delete district successfully")
    void testDeleteDistrictSuccess() {
        // Arrange
        when(districtRepository.findById(1L)).thenReturn(Optional.of(testDistrict));

        // Act
        districtService.deleteDistrict(1L);

        // Assert
        verify(districtRepository).delete(testDistrict);
    }

    @Test
    @DisplayName("Get districts below sustainability threshold")
    void testGetDistrictsBelowSustainabilityThreshold() {
        // Arrange
        District lowSustainabilityDistrict = District.builder()
            .id(2L)
            .name("Low Sustainability District")
            .population(30000)
            .sustainabilityScore(45.0)
            .operationalRiskScore(50.0)
            .build();

        when(districtRepository.findBelowSustainabilityThreshold(50.0))
            .thenReturn(List.of(lowSustainabilityDistrict));

        // Act
        List<DistrictResponse> response = districtService.getDistrictsBelowSustainabilityThreshold(50.0);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(45.0, response.get(0).getSustainabilityScore());
    }

    @Test
    @DisplayName("Get districts by highest risk")
    void testGetDistrictsByHighestRisk() {
        // Arrange
        District highRiskDistrict = District.builder()
            .id(3L)
            .name("High Risk District")
            .population(20000)
            .sustainabilityScore(55.0)
            .operationalRiskScore(85.0)
            .build();

        when(districtRepository.findByHighestRisk()).thenReturn(List.of(highRiskDistrict, testDistrict));

        // Act
        List<DistrictResponse> response = districtService.getDistrictsByHighestRisk();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(85.0, response.get(0).getOperationalRiskScore());
    }
}
