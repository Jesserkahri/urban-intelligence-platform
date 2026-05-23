package com.urban.intelligence.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urban.intelligence.platform.auth.dto.LoginRequest;
import com.urban.intelligence.platform.auth.dto.RegisterRequest;
import com.urban.intelligence.platform.dto.DistrictCreateRequest;
import com.urban.intelligence.platform.dto.DistrictUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for District CRUD operations.
 * Tests creation, fetch, update via authenticated requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DistrictIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register an admin user (default role is VIEWER, but good enough for auth test)
        RegisterRequest register = RegisterRequest.builder()
            .username("districtadmin")
            .email("districtadmin@example.com")
            .password("password123")
            .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andReturn();

        adminToken = "Bearer " + objectMapper.readTree(result.getResponse().getContentAsString())
            .get("data").get("access_token").asText();
    }

    @Test
    void createDistrict_shouldReturnDistrict() throws Exception {
        DistrictCreateRequest request = DistrictCreateRequest.builder()
            .name("Test District")
            .population(50000)
            .sustainabilityScore(75.0)
            .operationalRiskScore(30.0)
            .build();

        mockMvc.perform(post("/api/districts")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Test District"))
            .andExpect(jsonPath("$.data.population").value(50000))
            .andExpect(jsonPath("$.message").value("District created successfully"));
    }

    @Test
    void getDistrict_shouldReturnDistrict() throws Exception {
        // Create first
        DistrictCreateRequest create = DistrictCreateRequest.builder()
            .name("Fetch District")
            .population(30000)
            .sustainabilityScore(80.0)
            .operationalRiskScore(20.0)
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/districts")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andReturn();

        Long districtId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("data").get("id").asLong();

        // Fetch
        mockMvc.perform(get("/api/districts/" + districtId)
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Fetch District"))
            .andExpect(jsonPath("$.data.population").value(30000));
    }

    @Test
    void updateDistrict_shouldReturnUpdatedDistrict() throws Exception {
        // Create
        DistrictCreateRequest create = DistrictCreateRequest.builder()
            .name("Update District")
            .population(40000)
            .sustainabilityScore(70.0)
            .operationalRiskScore(40.0)
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/districts")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andReturn();

        Long districtId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("data").get("id").asLong();

        // Update
        DistrictUpdateRequest update = DistrictUpdateRequest.builder()
            .name("Updated District")
            .population(45000)
            .sustainabilityScore(85.0)
            .operationalRiskScore(25.0)
            .build();

        mockMvc.perform(put("/api/districts/" + districtId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Updated District"))
            .andExpect(jsonPath("$.message").value("District updated successfully"));
    }

    @Test
    void getDistrict_withoutAuth_shouldFail() throws Exception {
        mockMvc.perform(get("/api/districts/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteDistrict_shouldSucceed() throws Exception {
        // Create
        DistrictCreateRequest create = DistrictCreateRequest.builder()
            .name("Delete District")
            .population(10000)
            .sustainabilityScore(60.0)
            .operationalRiskScore(50.0)
            .build();

        MvcResult createResult = mockMvc.perform(post("/api/districts")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andReturn();

        Long districtId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("data").get("id").asLong();

        // Delete
        mockMvc.perform(delete("/api/districts/" + districtId)
                .header("Authorization", adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("District deleted successfully"));
    }
}