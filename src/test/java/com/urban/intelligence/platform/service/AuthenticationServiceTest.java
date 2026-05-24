package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.dto.LoginRequest;
import com.urban.intelligence.platform.auth.dto.RegisterRequest;
import com.urban.intelligence.platform.auth.dto.RefreshTokenRequest;
import com.urban.intelligence.platform.auth.dto.TokenResponse;
import com.urban.intelligence.platform.auth.domain.session.RefreshTokenSession;
import com.urban.intelligence.platform.auth.repository.RefreshTokenSessionRepository;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import com.urban.intelligence.platform.auth.service.AuthenticationService;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenSessionRepository sessionRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Counter authLoginSuccessCounter;

    @Mock
    private Counter authLoginFailureCounter;

    private PasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
        authenticationService = new AuthenticationService(
            userRepository, sessionRepository, passwordEncoder,
            jwtTokenProvider, authLoginSuccessCounter, authLoginFailureCounter);

        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .displayName("Test User")
            .role(Role.VIEWER)
            .emailVerified(true)
            .failedLoginAttempts(0)
            .build();
    }

    @Test
    @DisplayName("Register successful with valid input")
    void testRegisterSuccess() {
        RegisterRequest req = RegisterRequest.builder()
            .username("newuser").email("new@test.com")
            .password("securepass123").build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(sessionRepository.save(any(RefreshTokenSession.class))).thenReturn(new RefreshTokenSession());

        TokenResponse response = authenticationService.register(req, "device-info");

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(RefreshTokenSession.class));
    }

    @Test
    @DisplayName("Login successful with valid credentials")
    void testLoginSuccess() {
        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser").password("password123").build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(sessionRepository.save(any(RefreshTokenSession.class))).thenReturn(new RefreshTokenSession());

        TokenResponse response = authenticationService.login(loginRequest, "device-info");

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(authLoginSuccessCounter).increment();
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void testLoginFailsWrongPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser").password("wrongpassword").build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        assertThrows(BadCredentialsException.class, () ->
            authenticationService.login(loginRequest, "device-info"));

        // saveAndFlush is called on failed attempt
        verify(userRepository).saveAndFlush(argThat(u -> u.getFailedLoginAttempts() > 0));
        verify(authLoginFailureCounter).increment();
    }

    @Test
    @DisplayName("Login fails when account locked")
    void testLoginFailsAccountLocked() {
        testUser.setFailedLoginAttempts(5);
        testUser.setLockedUntil(java.time.Instant.now().plusSeconds(3600));

        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser").password("password123").build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        assertThrows(BadCredentialsException.class, () ->
            authenticationService.login(loginRequest, "device-info"));
    }

    @Test
    @DisplayName("Refresh token creates new session")
    void testRefreshTokenSuccess() {
        String token = "old_refresh_token";
        RefreshTokenSession oldSession = RefreshTokenSession.builder()
            .id(1L).user(testUser)
            .token(AuthenticationService.hashToken(token))
            .expiresAt(java.time.Instant.now().plusSeconds(3600))
            .build();

        when(jwtTokenProvider.isRefreshToken(token)).thenReturn(true);
        when(sessionRepository.findByToken(AuthenticationService.hashToken(token)))
            .thenReturn(Optional.of(oldSession));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("new_access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("new_refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(sessionRepository.save(any(RefreshTokenSession.class))).thenReturn(new RefreshTokenSession());

        TokenResponse response = authenticationService.refreshToken(
            RefreshTokenRequest.builder().refreshToken(token).build(), "device-info");

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("new_refresh_token", response.getRefreshToken());
    }

    @Test
    @DisplayName("Logout revokes session")
    void testLogoutSuccess() {
        String token = "refresh_token";
        RefreshTokenSession session = RefreshTokenSession.builder()
            .id(1L).user(testUser)
            .token(AuthenticationService.hashToken(token))
            .build();

        when(sessionRepository.findByToken(AuthenticationService.hashToken(token)))
            .thenReturn(Optional.of(session));

        authenticationService.logout(token);

        verify(sessionRepository).save(argThat(s -> s.isRevoked()));
    }
}