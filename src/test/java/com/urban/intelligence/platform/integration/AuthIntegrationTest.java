package com.urban.intelligence.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urban.intelligence.platform.auth.dto.LoginRequest;
import com.urban.intelligence.platform.auth.dto.RefreshTokenRequest;
import com.urban.intelligence.platform.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication flow.
 * Tests register, login success, login failure, and token refresh.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturnTokens() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").exists())
            .andExpect(jsonPath("$.data.refresh_token").exists())
            .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void register_withDuplicateUsername_shouldFail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .username("dupuser")
            .email("dup@example.com")
            .password("password123")
            .build();

        // First registration
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // Duplicate username
        RegisterRequest duplicate = RegisterRequest.builder()
            .username("dupuser")
            .email("other@example.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void login_withValidCredentials_shouldReturnTokens() throws Exception {
        // First register a user
        RegisterRequest register = RegisterRequest.builder()
            .username("loginuser")
            .email("login@example.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        // Login
        LoginRequest login = LoginRequest.builder()
            .login("loginuser")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").exists())
            .andExpect(jsonPath("$.data.refresh_token").exists());
    }

    @Test
    void login_withWrongPassword_shouldFail() throws Exception {
        // First register
        RegisterRequest register = RegisterRequest.builder()
            .username("wpassuser")
            .email("wpass@example.com")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        // Wrong password
        LoginRequest login = LoginRequest.builder()
            .login("wpassuser")
            .password("wrongpassword")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    void refreshToken_withValidToken_shouldReturnNewTokens() throws Exception {
        // Register
        RegisterRequest register = RegisterRequest.builder()
            .username("refreshuser")
            .email("refresh@example.com")
            .password("password123")
            .build();

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
            .andReturn().getResponse().getContentAsString();

        // Extract refresh token from response
        String refreshToken = objectMapper.readTree(registerResponse)
            .get("data").get("refresh_token").asText();

        // Use it to refresh
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
            .refreshToken(refreshToken)
            .build();

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.access_token").exists())
            .andExpect(jsonPath("$.data.refresh_token").exists());
    }

    @Test
    void login_withNonexistentUser_shouldFail() throws Exception {
        LoginRequest login = LoginRequest.builder()
            .login("nobody")
            .password("password123")
            .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }
}