package com.urban.intelligence.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.dto.DistrictCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL-backed integration tests for District API.
 */
class DistrictIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        districtRepository.deleteAll();
        userRepository.deleteAll();

        User admin = User.builder()
            .username("admin").email("admin@test.com")
            .password("$2a$10$dummy") // not used for JWT generation
            .displayName("Admin").role(Role.ADMIN)
            .emailVerified(true).build();
        userRepository.save(admin);
        adminToken = jwtTokenProvider.generateAccessToken(admin);
    }

    private HttpEntity<?> authRequest(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    @DisplayName("GET /api/districts without auth returns 401")
    void getDistrict_withoutAuth_shouldFail() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/districts/1", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/districts creates district")
    void createDistrict_shouldReturnDistrict() throws Exception {
        DistrictCreateRequest req = DistrictCreateRequest.builder()
            .name("Integration District").population(50000)
            .sustainabilityScore(70.0).operationalRiskScore(30.0).build();

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/districts", HttpMethod.POST, authRequest(req), String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Integration District", responseData(response).path("name").asText());
    }

    @Test
    @DisplayName("GET /api/districts/{id} returns district")
    void getDistrict_shouldReturnDistrict() throws Exception {
        District district = districtRepository.save(District.builder()
            .name("TestDistrict").population(10000)
            .sustainabilityScore(60.0).operationalRiskScore(40.0).build());

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/districts/" + district.getId(),
            HttpMethod.GET, authRequest(null), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TestDistrict", responseData(response).path("name").asText());
    }

    @Test
    @DisplayName("DELETE /api/districts/{id} deletes district")
    void deleteDistrict_shouldSucceed() {
        District district = districtRepository.save(District.builder()
            .name("DeleteDistrict").population(5000)
            .sustainabilityScore(50.0).operationalRiskScore(50.0).build());

        ResponseEntity<Void> response = restTemplate.exchange(
            "/api/districts/" + district.getId(),
            HttpMethod.DELETE, authRequest(null), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(districtRepository.existsById(district.getId()));
    }

    @Test
    @DisplayName("PUT /api/districts/{id} updates district")
    void updateDistrict_shouldReturnUpdatedDistrict() throws Exception {
        District district = districtRepository.save(District.builder()
            .name("UpdateDistrict").population(10000)
            .sustainabilityScore(60.0).operationalRiskScore(40.0).build());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>("{\"population\": 20000, \"sustainabilityScore\": 80.0}", headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/districts/" + district.getId(),
            HttpMethod.PUT, req, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode data = responseData(response);
        assertEquals(20000, data.path("population").asInt());
        assertEquals(80.0, data.path("sustainability_score").asDouble());
    }

    private JsonNode responseData(ResponseEntity<String> response) throws Exception {
        assertNotNull(response.getBody());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertTrue(root.path("success").asBoolean());
        return root.path("data");
    }
}
