package com.urban.intelligence.platform.integration;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.dto.LoginRequest;
import com.urban.intelligence.platform.auth.dto.RefreshTokenRequest;
import com.urban.intelligence.platform.auth.dto.RegisterRequest;
import com.urban.intelligence.platform.auth.dto.TokenResponse;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import com.urban.intelligence.platform.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.urban.intelligence.platform.auth.repository.RefreshTokenSessionRepository;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL-backed integration tests for Authentication API.
 * All responses are wrapped in ApiResponse<T> per the controller contract.
 * Error responses return ApiError, not ApiResponse.
 */
class AuthIntegrationTest extends BaseIntegrationTest {
@Autowired
private RefreshTokenSessionRepository refreshTokenSessionRepository;  // ← ajoute
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final ParameterizedTypeReference<ApiResponse<TokenResponse>> TOKEN_RESPONSE =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<Void>> VOID_RESPONSE =
        new ParameterizedTypeReference<>() {};

@BeforeEach
void setUp() {
    refreshTokenSessionRepository.deleteAll();  // ← ajoute cette ligne
    userRepository.deleteAll();
}

    @Test
    @DisplayName("Register new user returns ApiResponse with tokens")
    void register_shouldReturnTokens() {
        RegisterRequest req = RegisterRequest.builder()
            .username("newuser").email("new@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<ApiResponse<TokenResponse>> response = restTemplate.exchange(
            "/api/auth/register", HttpMethod.POST,
            new HttpEntity<>(req), TOKEN_RESPONSE);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getData().getAccessToken());
        assertNotNull(response.getBody().getData().getRefreshToken());
        assertEquals("Bearer", response.getBody().getData().getTokenType());
    }

    @Test
    @DisplayName("Register with duplicate username fails with 400 ApiError")
    void register_withDuplicateUsername_shouldFail() {
        RegisterRequest req = RegisterRequest.builder()
            .username("dupuser").email("dup@test.com")
            .password("SecurePass1!").build();

        // First registration succeeds
        restTemplate.exchange("/api/auth/register", HttpMethod.POST,
            new HttpEntity<>(req), TOKEN_RESPONSE);

        // Second registration with same username fails — returns ApiError, not ApiResponse
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/auth/register", HttpMethod.POST,
            new HttpEntity<>(req), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        // ApiError contract: {"success":false,"code":"INVALID_ARGUMENT","message":"..."}
        assertTrue(response.getBody().contains("success"));
        assertTrue(response.getBody().contains("Username already taken"));
    }

    @Test
    @DisplayName("Login with valid credentials returns ApiResponse with tokens")
    void login_withValidCredentials_shouldSucceed() {
        User user = User.builder()
            .username("loginuser").email("login@test.com")
            .password(passwordEncoder.encode("SecurePass1!"))
            .displayName("Login User").role(Role.VIEWER)
            .emailVerified(true).build();
        userRepository.save(user);

        LoginRequest req = LoginRequest.builder()
            .login("loginuser").password("SecurePass1!").build();

        ResponseEntity<ApiResponse<TokenResponse>> response = restTemplate.exchange(
            "/api/auth/login", HttpMethod.POST,
            new HttpEntity<>(req), TOKEN_RESPONSE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getData().getAccessToken());
        assertEquals("Bearer", response.getBody().getData().getTokenType());
    }

    @Test
    @DisplayName("Login with wrong password returns 401 ApiError")
    void login_withWrongPassword_shouldFail() {
        User user = User.builder()
            .username("wrongpwd").email("wrong@test.com")
            .password(passwordEncoder.encode("SecurePass1!"))
            .displayName("Wrong Pwd").role(Role.VIEWER)
            .emailVerified(true).build();
        userRepository.save(user);

        LoginRequest req = LoginRequest.builder()
            .login("wrongpwd").password("WrongPassword1!").build();

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/auth/login", HttpMethod.POST,
            new HttpEntity<>(req), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Refresh token rotates the token pair")
    void refreshToken_withValidToken_shouldReturnNewTokens() {
        RegisterRequest regReq = RegisterRequest.builder()
            .username("refreshuser").email("refresh@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<ApiResponse<TokenResponse>> regResponse = restTemplate.exchange(
            "/api/auth/register", HttpMethod.POST,
            new HttpEntity<>(regReq), TOKEN_RESPONSE);
        String refreshToken = regResponse.getBody().getData().getRefreshToken();

        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
            .refreshToken(refreshToken).build();

        ResponseEntity<ApiResponse<TokenResponse>> refreshResponse = restTemplate.exchange(
            "/api/auth/refresh", HttpMethod.POST,
            new HttpEntity<>(refreshReq), TOKEN_RESPONSE);

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
        assertNotNull(refreshResponse.getBody().getData().getAccessToken());
        assertNotNull(refreshResponse.getBody().getData().getRefreshToken());
        assertNotEquals(refreshToken, refreshResponse.getBody().getData().getRefreshToken());
    }

    @Test
    @DisplayName("Revoked refresh token is rejected with 400 ApiError")
    void revokedRefreshToken_shouldBeRejected() {
        RegisterRequest regReq = RegisterRequest.builder()
            .username("revokeuser").email("revoke@test.com")
            .password("SecurePass1!").build();

        ResponseEntity<ApiResponse<TokenResponse>> regResponse = restTemplate.exchange(
            "/api/auth/register", HttpMethod.POST,
            new HttpEntity<>(regReq), TOKEN_RESPONSE);
        String refreshToken = regResponse.getBody().getData().getRefreshToken();

        // First refresh consumes the old token
        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
            .refreshToken(refreshToken).build();
        restTemplate.exchange("/api/auth/refresh", HttpMethod.POST,
            new HttpEntity<>(refreshReq), TOKEN_RESPONSE);

        // Second refresh with same (now revoked) token returns ApiError
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/auth/refresh", HttpMethod.POST,
            new HttpEntity<>(refreshReq), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}