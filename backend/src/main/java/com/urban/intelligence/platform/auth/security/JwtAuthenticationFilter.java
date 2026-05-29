package com.urban.intelligence.platform.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Processes incoming requests to validate JWT tokens.
 *
 * Extracts the JWT from the Authorization header, validates it,
 * and sets the Spring Security authentication context if valid.
 * Runs once per request to ensure stateless authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                Claims claims = jwtTokenProvider.validateToken(jwt);

                if ("refresh".equals(claims.get("type"))) {
                    log.debug("Refresh token detected in Authorization header - rejecting for API access");
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = claims.getSubject();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated user: {} with authorities: {}",
                            username, userDetails.getAuthorities());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Authentication failed: {}", e.getMessage());
            // Don't throw - let the filter chain continue and let Security handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

/**
 * Extract JWT token from the Authorization header.
 * Expected format: "Bearer <token>"
 */
private String extractJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
    }

    String queryToken = request.getParameter("access_token");
    if (StringUtils.hasText(queryToken) && request.getRequestURI().startsWith("/api/operations/stream")) {
        return queryToken;
    }

    return null;
}}

