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
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final LoginRateLimiter loginRateLimiter;

    /**
     * Get the currently authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User user) {
        log.debug("GET current user: {}", user.getUsername());
        UserResponse userResponse = UserResponse.from(user);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Current user retrieved"));
    }

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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        log.debug("REFRESH token request");
        String deviceInfo = httpRequest.getHeader("User-Agent");
        TokenResponse response = authenticationService.refreshToken(request, deviceInfo);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.debug("LOGOUT request");
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        log.info("PASSWORD RESET requested for: {}", request.getEmail());
        // Generate token but do NOT return it — in production this would be emailed
        authenticationService.verifyPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null,
            "If an account with that email exists, a reset link has been generated."));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        log.info("PASSWORD RESET confirmed");
        authenticationService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    @PostMapping("/admin/logout-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminLogoutAll(@AuthenticationPrincipal User user) {
        log.info("ADMIN logout all sessions for user: {}", user.getUsername());
        authenticationService.logoutAll(user);
        return ResponseEntity.ok(ApiResponse.success(null, "All sessions revoked"));
    }
}