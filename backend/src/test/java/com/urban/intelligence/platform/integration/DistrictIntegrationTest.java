package com.urban.intelligence.platform.integration;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.dto.RefreshTokenRequest;
import com.urban.intelligence.platform.auth.repository.RefreshTokenSessionRepository;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.dto.DistrictCreateRequest;
import com.urban.intelligence.platform.dto.DistrictResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL-backed integration tests for District API.
 * All responses are wrapped in ApiResponse<T> per the controller contract.
 */
class DistrictIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
private RefreshTokenSessionRepository refreshTokenSessionRepository;  // ← ajout
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private HttpHeaders authHeaders;

    private static final ParameterizedTypeReference<ApiResponse<DistrictResponse>> DISTRICT_RESPONSE =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<Void>> VOID_RESPONSE =
        new ParameterizedTypeReference<>() {};

    @BeforeEach
    void setUp() {
        districtRepository.deleteAll();
                refreshTokenSessionRepository.deleteAll();
        userRepository.deleteAll();
        User admin = User.builder()
            .username("admin").email("admin@test.com")
            .password("$2a$10$dummy")
            .displayName("Admin").role(Role.ADMIN)
            .emailVerified(true).build();
        userRepository.save(admin);
        adminToken = jwtTokenProvider.generateAccessToken(admin);

        authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(adminToken);
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    private <T> HttpEntity<T> authEntity(T body) {
        return new HttpEntity<>(body, authHeaders);
    }

    @Test
    @DisplayName("GET /api/districts without auth returns 401 ApiError")
    void getDistrict_withoutAuth_shouldFail() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/districts/1", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/districts creates district and returns ApiResponse")
    void createDistrict_shouldReturnDistrict() {
        DistrictCreateRequest req = DistrictCreateRequest.builder()
            .name("Integration District").population(50000)
            .sustainabilityScore(70.0).operationalRiskScore(30.0).build();

        ResponseEntity<ApiResponse<DistrictResponse>> response = restTemplate.exchange(
            "/api/districts", HttpMethod.POST, authEntity(req), DISTRICT_RESPONSE);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals("Integration District", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("GET /api/districts/{id} returns ApiResponse with district")
    void getDistrict_shouldReturnDistrict() {
        District district = districtRepository.save(District.builder()
            .name("TestDistrict").population(10000)
            .sustainabilityScore(60.0).operationalRiskScore(40.0).build());

        ResponseEntity<ApiResponse<DistrictResponse>> response = restTemplate.exchange(
            "/api/districts/" + district.getId(),
            HttpMethod.GET, authEntity(null), DISTRICT_RESPONSE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("TestDistrict", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("DELETE /api/districts/{id} returns 200 ApiResponse (not 204)")
    void deleteDistrict_shouldSucceed() {
        District district = districtRepository.save(District.builder()
            .name("DeleteDistrict").population(5000)
            .sustainabilityScore(50.0).operationalRiskScore(50.0).build());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
            "/api/districts/" + district.getId(),
            HttpMethod.DELETE, authEntity(null), VOID_RESPONSE);

        // Controller returns 200 ApiResponse<Void>, not 204
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertFalse(districtRepository.existsById(district.getId()));
    }

    @Test
    @DisplayName("PUT /api/districts/{id} updates district and returns ApiResponse")
    void updateDistrict_shouldReturnUpdatedDistrict() {
        District district = districtRepository.save(District.builder()
            .name("UpdateDistrict").population(10000)
            .sustainabilityScore(60.0).operationalRiskScore(40.0).build());

        HttpEntity<String> req = new HttpEntity<>(
            "{\"population\": 20000, \"sustainabilityScore\": 80.0}", authHeaders);

        ResponseEntity<ApiResponse<DistrictResponse>> response = restTemplate.exchange(
            "/api/districts/" + district.getId(),
            HttpMethod.PUT, req, DISTRICT_RESPONSE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(20000, response.getBody().getData().getPopulation());
        assertEquals(80.0, response.getBody().getData().getSustainabilityScore());
    }
}