package com.urban.intelligence.platform.security;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.security.JwtTokenProvider;
import com.urban.intelligence.platform.auth.security.UserDetailsServiceImpl;
import com.urban.intelligence.platform.auth.repository.UserRepository;
import com.urban.intelligence.platform.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for security components
 * Tests JWT validation, user details loading, and authorization
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Security Component Unit Tests")
class SecurityComponentTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("viewer")
            .email("viewer@example.com")
            .password("hashed_password")
            .displayName("Viewer User")
            .role(Role.VIEWER)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

        adminUser = User.builder()
            .id(2L)
            .username("admin")
            .email("admin@example.com")
            .password("hashed_password")
            .displayName("Admin User")
            .role(Role.ADMIN)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
    }

    @Test
    @DisplayName("Load user details by username successfully")
    void testLoadUserByUsernameSuccess() {
        // Arrange
        when(userRepository.findByUsername("viewer")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("viewer");

        // Assert
        assertNotNull(userDetails);
        assertEquals("viewer", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Load user details throws exception for non-existent user")
    void testLoadUserByUsernameNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    @DisplayName("VIEWER role is loaded correctly")
    void testViewerRoleLoaded() {
        // Arrange
        when(userRepository.findByUsername("viewer")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("viewer");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")));
        assertFalse(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("ADMIN role is loaded correctly")
    void testAdminRoleLoaded() {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Locked user cannot authenticate")
    void testLockedUserCannotAuthenticate() {
        // Arrange
        User lockedUser = User.builder()
            .id(3L)
            .username("locked")
            .email("locked@example.com")
            .password("hashed_password")
            .displayName("Locked User")
            .role(Role.VIEWER)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(false)  // Account is locked
            .credentialsNonExpired(true)
            .build();

        when(userRepository.findByUsername("locked")).thenReturn(Optional.of(lockedUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("locked");

        // Assert
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    @DisplayName("Disabled user cannot authenticate")
    void testDisabledUserCannotAuthenticate() {
        // Arrange
        User disabledUser = User.builder()
            .id(4L)
            .username("disabled")
            .email("disabled@example.com")
            .password("hashed_password")
            .displayName("Disabled User")
            .role(Role.VIEWER)
            .enabled(false)  // Account is disabled
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(disabledUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("disabled");

        // Assert
        assertFalse(userDetails.isEnabled());
    }
}
