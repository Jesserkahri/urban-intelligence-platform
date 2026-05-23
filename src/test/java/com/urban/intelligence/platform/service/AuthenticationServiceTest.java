package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.dto.LoginRequest;
import com.urban.intelligence.platform.auth.dto.RegisterRequest;
import com.urban.intelligence.platform.auth.dto.RefreshTokenRequest;
import com.urban.intelligence.platform.auth.repository.RefreshTokenSessionRepository;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import com.urban.intelligence.platform.auth.service.AuthenticationService;
import com.urban.intelligence.platform.auth.dto.TokenResponse;
import com.urban.intelligence.platform.auth.domain.session.RefreshTokenSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 * Tests registration, login, token refresh, and account lockout
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenSessionRepository sessionRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Use real BCryptPasswordEncoder for consistent behavior
        passwordEncoder = new BCryptPasswordEncoder(12);
        
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

        registerRequest = RegisterRequest.builder()
            .username("newuser")
            .email("newuser@example.com")
            .password("securepass123")
            .displayName("New User")
            .build();
    }

    @Test
    @DisplayName("Register successful with valid input")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(sessionRepository.save(any(RefreshTokenSession.class))).thenReturn(new RefreshTokenSession());

        // Act
        TokenResponse response = authenticationService.register(registerRequest, "device-info");

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository).save(any(User.class));
        verify(sessionRepository).save(any(RefreshTokenSession.class));
    }

    @Test
    @DisplayName("Register fails with duplicate username")
    void testRegisterFailsDuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(registerRequest, "device-info");
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register fails with duplicate email")
    void testRegisterFailsDuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(registerRequest, "device-info");
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login successful with valid credentials")
    void testLoginSuccess() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser")
            .password("password123")
            .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(sessionRepository.save(any(RefreshTokenSession.class))).thenReturn(new RefreshTokenSession());

        // Act
        TokenResponse response = authenticationService.login(loginRequest, "device-info");

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(userRepository).save(argThat(u -> u.getFailedLoginAttempts() == 0));
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void testLoginFailsWrongPassword() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser")
            .password("wrongpassword")
            .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginRequest, "device-info");
        });
        verify(userRepository).save(argThat(u -> u.getFailedLoginAttempts() > 0));
    }

    @Test
    @DisplayName("Login fails with non-existent user")
    void testLoginFailsUserNotFound() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
            .login("nonexistent")
            .password("password123")
            .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginRequest, "device-info");
        });
    }

    @Test
    @DisplayName("Login fails when email not verified")
    void testLoginFailsEmailNotVerified() {
        // Arrange
        testUser.setEmailVerified(false);
        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser")
            .password("password123")
            .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginRequest, "device-info");
        });
    }

    @Test
    @DisplayName("Login fails when account locked")
    void testLoginFailsAccountLocked() {
        // Arrange
        testUser.setFailedLoginAttempts(5);
        testUser.setLockedUntil(java.time.Instant.now().plusSeconds(3600));

        LoginRequest loginRequest = LoginRequest.builder()
            .login("testuser")
            .password("password123")
            .build();

        when(userRepository.findByUsernameOrEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.login(loginRequest, "device-info");
        });
    }

    @Test
    @DisplayName("Refresh token creates new session")
    void testRefreshTokenSuccess() {
        // Arrange
        RefreshTokenSession oldSession = RefreshTokenSession.builder()
            .id(1L)
            .user(testUser)
            .token("old_refresh_token")
            .expiresAt(java.time.Instant.now().plusSeconds(3600))
            .build();

        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
            .refreshToken("old_refresh_token")
            .build();

        when(jwtTokenProvider.isRefreshToken("old_refresh_token")).thenReturn(true);
        when(sessionRepository.findByToken("old_refresh_token")).thenReturn(Optional.of(oldSession));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("new_access_token");
        when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("new_refresh_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(sessionRepository.save(any(RefreshTokenSession.class))).thenReturn(new RefreshTokenSession());

        // Act
        TokenResponse response = authenticationService.refreshToken(refreshRequest, "device-info");

        // Assert
        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("new_refresh_token", response.getRefreshToken());
        // Verify old session was revoked
        verify(sessionRepository, times(2)).save(argThat(s -> s.isRevoked()));
    }

    @Test
    @DisplayName("Logout revokes session")
    void testLogoutSuccess() {
        // Arrange
        RefreshTokenSession session = RefreshTokenSession.builder()
            .id(1L)
            .user(testUser)
            .token("refresh_token")
            .build();

        when(sessionRepository.findByToken("refresh_token")).thenReturn(Optional.of(session));

        // Act
        authenticationService.logout("refresh_token");

        // Assert
        verify(sessionRepository).save(argThat(s -> s.isRevoked()));
    }
}
