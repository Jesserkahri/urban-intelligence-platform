package com.urban.intelligence.platform.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.dto.LoginRequest;
import com.urban.intelligence.platform.auth.dto.RefreshTokenRequest;
import com.urban.intelligence.platform.auth.dto.RegisterRequest;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL-backed integration tests for Authentication API.
 * Tests registration, login, refresh token rotation, and error handling.
 */
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Register new user returns tokens")
    void register_shouldReturnTokens() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
            .username("newuser").email("new@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/register", req, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        JsonNode data = responseData(response);
        assertTrue(data.path("access_token").isTextual());
        assertTrue(data.path("refresh_token").isTextual());
        assertEquals("Bearer", data.path("token_type").asText());
    }

    @Test
    @DisplayName("Register with duplicate username fails")
    void register_withDuplicateUsername_shouldFail() {
        RegisterRequest req = RegisterRequest.builder()
            .username("dupuser").email("dup@test.com")
            .password("SecurePass1!").build();

        // First registration succeeds
        restTemplate.postForEntity("/api/auth/register", req, String.class);

        // Second registration with same username fails
        RegisterRequest dupReq = RegisterRequest.builder()
            .username("dupuser").email("other@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/register", dupReq, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Login with valid credentials returns tokens")
    void login_withValidCredentials_shouldSucceed() throws Exception {
        // Pre-register user directly
        User user = User.builder()
            .username("loginuser").email("login@test.com")
            .password(passwordEncoder.encode("SecurePass1!"))
            .displayName("Login User").role(Role.VIEWER)
            .emailVerified(true).build();
        userRepository.save(user);

        LoginRequest req = LoginRequest.builder()
            .login("loginuser").password("SecurePass1!").build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/login", req, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(responseData(response).path("access_token").isTextual());
    }

    @Test
    @DisplayName("Login with wrong password fails")
    void login_withWrongPassword_shouldFail() {
        User user = User.builder()
            .username("wrongpwd").email("wrong@test.com")
            .password(passwordEncoder.encode("SecurePass1!"))
            .displayName("Wrong Pwd").role(Role.VIEWER)
            .emailVerified(true).build();
        userRepository.save(user);

        LoginRequest req = LoginRequest.builder()
            .login("wrongpwd").password("WrongPassword1!").build();

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/auth/login", req, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Refresh token rotates the token pair")
    void refreshToken_withValidToken_shouldReturnNewTokens() throws Exception {
        // Register a user first
        RegisterRequest regReq = RegisterRequest.builder()
            .username("refreshuser").email("refresh@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<String> regResponse = restTemplate.postForEntity(
            "/api/auth/register", regReq, String.class);
        String refreshToken = responseData(regResponse).path("refresh_token").asText();

        // Refresh the token
        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
            .refreshToken(refreshToken).build();

        ResponseEntity<String> refreshResponse = restTemplate.postForEntity(
            "/api/auth/refresh", refreshReq, String.class);

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        JsonNode data = responseData(refreshResponse);
        assertTrue(data.path("access_token").isTextual());
        assertTrue(data.path("refresh_token").isTextual());
        assertNotEquals(refreshToken, data.path("refresh_token").asText());
    }

    @Test
    @DisplayName("Revoked refresh token is rejected")
    void revokedRefreshToken_shouldBeRejected() throws Exception {
        RegisterRequest regReq = RegisterRequest.builder()
            .username("revokeuser").email("revoke@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<String> regResponse = restTemplate.postForEntity(
            "/api/auth/register", regReq, String.class);
        String refreshToken = responseData(regResponse).path("refresh_token").asText();

        // First refresh (consumes old token)
        RefreshTokenRequest refreshReq1 = RefreshTokenRequest.builder()
            .refreshToken(refreshToken).build();
        restTemplate.postForEntity("/api/auth/refresh", refreshReq1, TokenResponse.class);

        // Second refresh with same token should fail
        ResponseEntity<String> refreshReq2 = restTemplate.postForEntity(
            "/api/auth/refresh", refreshReq1, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, refreshReq2.getStatusCode());
    }

    private JsonNode responseData(ResponseEntity<String> response) throws Exception {
        assertNotNull(response.getBody());
        JsonNode root = objectMapper.readTree(response.getBody());
        assertTrue(root.path("success").asBoolean());
        return root.path("data");
    }

    @Test
    @DisplayName("Invalid JWT is rejected")
    void invalidJwt_shouldBeRejected() {
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/districts/1",
            HttpMethod.GET,
            new HttpEntity<>(null, org.springframework.http.HttpHeaders.EMPTY),
            String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
