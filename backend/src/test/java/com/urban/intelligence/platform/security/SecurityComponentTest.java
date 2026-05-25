package com.urban.intelligence.platform.security;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.security.UserDetailsServiceImpl;
import com.urban.intelligence.platform.auth.repository.UserRepository;
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
 * Unit tests for UserDetailsServiceImpl security component.
 * Tests user loading by username or email.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Security Component Unit Tests")
class SecurityComponentTest {

    @Mock
    private UserRepository userRepository;

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
        when(userRepository.findByUsernameOrEmail("viewer", "viewer")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("viewer");

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
        when(userRepository.findByUsernameOrEmail("nonexistent", "nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    @DisplayName("VIEWER role is loaded correctly")
    void testViewerRoleLoaded() {
        when(userRepository.findByUsernameOrEmail("viewer", "viewer")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("viewer");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertNotNull(authorities);
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")));
        assertFalse(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("ADMIN role is loaded correctly")
    void testAdminRoleLoaded() {
        when(userRepository.findByUsernameOrEmail("admin", "admin")).thenReturn(Optional.of(adminUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertNotNull(authorities);
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Locked user cannot authenticate")
    void testLockedUserCannotAuthenticate() {
        User lockedUser = User.builder()
            .id(3L)
            .username("locked")
            .email("locked@example.com")
            .password("hashed_password")
            .displayName("Locked User")
            .role(Role.VIEWER)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(false)
            .credentialsNonExpired(true)
            .build();

        when(userRepository.findByUsernameOrEmail("locked", "locked")).thenReturn(Optional.of(lockedUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("locked");

        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    @DisplayName("Disabled user cannot authenticate")
    void testDisabledUserCannotAuthenticate() {
        User disabledUser = User.builder()
            .id(4L)
            .username("disabled")
            .email("disabled@example.com")
            .password("hashed_password")
            .displayName("Disabled User")
            .role(Role.VIEWER)
            .enabled(false)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

        when(userRepository.findByUsernameOrEmail("disabled", "disabled")).thenReturn(Optional.of(disabledUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("disabled");

        assertFalse(userDetails.isEnabled());
    }
}