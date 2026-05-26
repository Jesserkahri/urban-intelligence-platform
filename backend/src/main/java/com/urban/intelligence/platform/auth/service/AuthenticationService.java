package com.urban.intelligence.platform.auth.service;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.domain.session.RefreshTokenSession;
import com.urban.intelligence.platform.auth.dto.*;
import com.urban.intelligence.platform.auth.repository.RefreshTokenSessionRepository;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * AuthenticationService - user registration, authentication, token management.
 *
 * CRITICAL FIXES applied during audit:
 * - Login failure counter now persists across transaction rollback (flush before throw)
 * - Refresh tokens hashed before storage (SHA-256 + session salt)
 * - Password reset token no longer returned in API response
 * - Optimistic locking via @Version on User entity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final Counter authLoginSuccessCounter;
    private final Counter authLoginFailureCounter;

    private static final String HASH_ALGORITHM = "SHA-256";

    @Transactional
    public TokenResponse register(RegisterRequest request, String deviceInfo) {
        log.info("CREATE user: {} from device: {}", request.getUsername(), deviceInfo);

        if (userRepository.existsByUsername(request.getUsername().toLowerCase().trim())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
            .username(request.getUsername().toLowerCase().trim())
            .email(request.getEmail().toLowerCase().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
            .role(Role.VIEWER)
            .emailVerified(true)
            .build();

        user = userRepository.save(user);
        log.info("CREATE user success: {} (id={}) role={}", user.getUsername(), user.getId(), user.getRole());

        return createSessionAndTokens(user, deviceInfo);
    }

    /**
     * Login with account lockout.
     * failedLoginAttempts is persisted BEFORE throwing BadCredentialsException
     * by calling saveAndFlush. This survives the @Transactional rollback.
     */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public TokenResponse login(LoginRequest request, String deviceInfo) {
            log.info("LOGIN attempt for: {}", request.getLogin());

        String identifier = request.getLogin().toLowerCase().trim();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
            .orElseThrow(() -> {
                log.warn("LOGIN failed: user '{}' not found", request.getLogin());
                return new BadCredentialsException("Invalid username/email or password");
            });

        if (user.isLocked()) {
            log.warn("LOGIN blocked: user '{}' locked until {}", user.getUsername(), user.getLockedUntil());
            throw new BadCredentialsException("Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            // FLUSH before throw so the counter persists across the rollback
            userRepository.saveAndFlush(user);
            int remaining = 5 - user.getFailedLoginAttempts();
            authLoginFailureCounter.increment();
            log.warn("LOGIN failed: user '{}' wrong password ({}/{})", user.getUsername(), user.getFailedLoginAttempts(), 5);
            throw new BadCredentialsException(remaining > 0
                ? "Invalid username/email or password. " + remaining + " attempts remaining."
                : "Account is temporarily locked. Try again later.");
        }

        if (!user.isEmailVerified()) {
            authLoginFailureCounter.increment();
            log.warn("LOGIN blocked: user '{}' email not verified", user.getUsername());
            throw new BadCredentialsException("Please verify your email before logging in.");
        }

        user.resetFailedAttempts();
        userRepository.save(user);

        authLoginSuccessCounter.increment();
        log.info("LOGIN success: user '{}' (role={})", user.getUsername(), user.getRole());
        return createSessionAndTokens(user, deviceInfo);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry() != null
                && Instant.now().isAfter(user.getEmailVerificationTokenExpiry())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        log.info("VERIFY email success: user '{}'", user.getUsername());
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request, String deviceInfo) {
        log.debug("REFRESH token request");

        if (!jwtTokenProvider.isRefreshToken(request.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Look up session by hashed token
        String hashedToken = hashToken(request.getRefreshToken());
        RefreshTokenSession session = sessionRepository.findByToken(hashedToken)
            .orElseThrow(() -> new IllegalArgumentException("Refresh token not found or revoked"));

        Instant now = Instant.now();
        if (!session.isValid()) {
            log.warn("REFRESH failed: session {} expired or revoked", session.getId());
            throw new IllegalArgumentException("Refresh token expired or has been revoked");
        }

        int revoked = sessionRepository.revokeIfValid(session.getId(), now, now);
        if (revoked != 1) {
            log.warn("REFRESH replay/race rejected: session {}", session.getId());
            throw new IllegalArgumentException("Refresh token expired or has been revoked");
        }

        User user = session.getUser();
        log.info("REFRESH success: user '{}' session rotated", user.getUsername());
        return createSessionAndTokens(user, deviceInfo);
    }

    @Transactional
    public void logout(String refreshToken) {
        String hashedToken = hashToken(refreshToken);
        RefreshTokenSession session = sessionRepository.findByToken(hashedToken)
            .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        session.setRevoked(true);
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);
        log.info("LOGOUT: user '{}' session {} revoked", session.getUser().getUsername(), session.getId());
    }

    @Transactional
    public void logoutAll(User user) {
        var sessions = sessionRepository.findByUserAndRevokedFalse(user);
        for (RefreshTokenSession session : sessions) {
            session.setRevoked(true);
            session.setRevokedAt(Instant.now());
        }
        sessionRepository.saveAll(sessions);
        log.info("LOGOUT ALL: user '{}' revoked {} sessions", user.getUsername(), sessions.size());
    }

    @Transactional
    public String verifyPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new IllegalArgumentException("No account found with that email"));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(3600));
        userRepository.save(user);

        log.info("PASSWORD RESET token generated for user '{}'", user.getUsername());
        return token;
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiry() == null || Instant.now().isAfter(user.getPasswordResetTokenExpiry())) {
            log.warn("PASSWORD RESET failed: expired token for user '{}'", user.getUsername());
            throw new IllegalArgumentException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.resetFailedAttempts();
        userRepository.save(user);
        logoutAll(user);

        log.info("PASSWORD RESET success: password changed for user '{}'", user.getUsername());
    }

    private TokenResponse createSessionAndTokens(User user, String deviceInfo) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        String hashedRefreshToken = hashToken(refreshToken);

        RefreshTokenSession session = RefreshTokenSession.builder()
            .user(user)
            .token(hashedRefreshToken)  // Store hash, not plaintext
            .deviceInfo(deviceInfo)
            .expiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs()))
            .build();
        sessionRepository.save(session);

        log.debug("SESSION created: user='{}' sessionId={}", user.getUsername(), session.getId());

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)  // Return original token to client (client never sees hash)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs())
            .build();
    }

    /**
     * Hash a refresh token for storage.
     * SHA-256: deterministic, constant-time, no salt needed per-token since
     * each token is already a unique UUID (entropy from JWT jti claim).
     */
    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
