package com.urban.intelligence.platform.auth.api;

import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.dto.*;
import com.urban.intelligence.platform.auth.security.LoginRateLimiter;
import com.urban.intelligence.platform.auth.service.AuthenticationService;
import com.urban.intelligence.platform.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AuthenticationController - REST endpoints for auth flows.
 *
 * Public:  register, login, refresh, password-reset
 * Authenticated: logout
 * Admin:  admin operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final LoginRateLimiter loginRateLimiter;

    /**
     * Register a new user account.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        log.info("CREATE user registration: {}", request.getUsername());
        String deviceInfo = httpRequest.getHeader("User-Agent");
        TokenResponse response = authenticationService.register(request, deviceInfo);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Authenticate user and receive token pair.
     * POST /api/auth/login
     *
     * Rate-limited per IP address (5 attempts/minute).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        log.debug("LOGIN attempt from IP: {}", ip);

        if (!loginRateLimiter.isAllowed(ip)) {
            log.warn("LOGIN rate limited IP: {}", ip);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error("Too many login attempts. Try again later."));
        }

        loginRateLimiter.recordAttempt(ip);
        String deviceInfo = httpRequest.getHeader("User-Agent");
        TokenResponse response = authenticationService.login(request, deviceInfo);
        loginRateLimiter.reset(ip);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Refresh access token using refresh token.
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        log.debug("REFRESH token request");
        String deviceInfo = httpRequest.getHeader("User-Agent");
        TokenResponse response = authenticationService.refreshToken(request, deviceInfo);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Logout — revoke the current refresh token session.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.debug("LOGOUT request");
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    // ====== Password Reset ======

    /**
     * Request a password reset token.
     * POST /api/auth/password-reset/request
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        log.info("PASSWORD RESET requested for: {}", request.getEmail());
        String token = authenticationService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(token, "Password reset token generated. In production, this would be emailed."));
    }

    /**
     * Confirm password reset with token.
     * POST /api/auth/password-reset/confirm
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        log.info("PASSWORD RESET confirmed");
        authenticationService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    // ====== Admin endpoints ======

    /**
     * Logout all sessions for a user (admin only).
     * POST /api/auth/admin/logout-all
     */
    @PostMapping("/admin/logout-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminLogoutAll(@AuthenticationPrincipal User user) {
        log.info("ADMIN logout all sessions for user: {}", user.getUsername());
        authenticationService.logoutAll(user);
        return ResponseEntity.ok(ApiResponse.success(null, "All sessions revoked"));
    }
}