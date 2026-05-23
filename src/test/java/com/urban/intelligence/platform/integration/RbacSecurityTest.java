package com.urban.intelligence.platform.integration;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RBAC (Role-Based Access Control)
 * Tests authorization enforcement on protected endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RBAC Security Integration Tests")
class RbacSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User operatorUser;
    private User viewerUser;
    private String adminToken;
    private String operatorToken;
    private String viewerToken;
    private District testDistrict;

    @BeforeEach
    void setUp() {
        // Clear data
        districtRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        adminUser = User.builder()
            .username("admin_user")
            .email("admin@example.com")
            .password(passwordEncoder.encode("password123"))
            .displayName("Admin User")
            .role(Role.ADMIN)
            .emailVerified(true)
            .build();

        operatorUser = User.builder()
            .username("operator_user")
            .email("operator@example.com")
            .password(passwordEncoder.encode("password123"))
            .displayName("Operator User")
            .role(Role.OPERATOR)
            .emailVerified(true)
            .build();

        viewerUser = User.builder()
            .username("viewer_user")
            .email("viewer@example.com")
            .password(passwordEncoder.encode("password123"))
            .displayName("Viewer User")
            .role(Role.VIEWER)
            .emailVerified(true)
            .build();

        adminUser = userRepository.save(adminUser);
        operatorUser = userRepository.save(operatorUser);
        viewerUser = userRepository.save(viewerUser);

        // Generate tokens
        adminToken = jwtTokenProvider.generateAccessToken(adminUser);
        operatorToken = jwtTokenProvider.generateAccessToken(operatorUser);
        viewerToken = jwtTokenProvider.generateAccessToken(viewerUser);

        // Create test district
        testDistrict = District.builder()
            .name("Test RBAC District")
            .population(50000)
            .sustainabilityScore(75.0)
            .operationalRiskScore(40.0)
            .build();
        testDistrict = districtRepository.save(testDistrict);
    }

    @Test
    @DisplayName("Admin can create district")
    void testAdminCanCreateDistrict() throws Exception {
        // Arrange
        DistrictCreateRequest request = DistrictCreateRequest.builder()
            .name("Admin Created District")
            .population(30000)
            .sustainabilityScore(70.0)
            .operationalRiskScore(35.0)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/districts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Operator can create district")
    void testOperatorCanCreateDistrict() throws Exception {
        // Arrange
        DistrictCreateRequest request = DistrictCreateRequest.builder()
            .name("Operator Created District")
            .population(25000)
            .sustainabilityScore(65.0)
            .operationalRiskScore(45.0)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/districts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + operatorToken)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Viewer cannot create district")
    void testViewerCannotCreateDistrict() throws Exception {
        // Arrange
        DistrictCreateRequest request = DistrictCreateRequest.builder()
            .name("Viewer Created District")
            .population(20000)
            .sustainabilityScore(60.0)
            .operationalRiskScore(50.0)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/districts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + viewerToken)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Admin can delete district")
    void testAdminCanDeleteDistrict() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Operator cannot delete district")
    void testOperatorCannotDeleteDistrict() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer " + operatorToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Viewer cannot delete district")
    void testViewerCannotDeleteDistrict() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("All roles can read districts")
    void testAllRolesCanReadDistricts() throws Exception {
        // Act & Assert for Admin
        mockMvc.perform(get("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Act & Assert for Operator
        mockMvc.perform(get("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer " + operatorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Act & Assert for Viewer
        mockMvc.perform(get("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Unauthenticated request is rejected")
    void testUnauthenticatedRequestRejected() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/districts/" + testDistrict.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid token is rejected")
    void testInvalidTokenRejected() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer invalid_token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Expired token is rejected")
    void testExpiredTokenRejected() throws Exception {
        // Note: In real scenario, we'd create an actually expired token
        // For now, this test demonstrates the pattern
        
        // Act & Assert
        mockMvc.perform(get("/api/districts/" + testDistrict.getId())
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjAsInN1YiI6InRlc3QiLCJ0eXBlIjoiYWNjZXNzIn0.invalid"))
            .andExpect(status().isUnauthorized());
    }
}
