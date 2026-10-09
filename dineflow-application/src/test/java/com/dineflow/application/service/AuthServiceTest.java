package com.dineflow.application.service;

import com.dineflow.application.dto.AccessToken;
import com.dineflow.application.exception.AuthenticationFailedException;
import com.dineflow.application.exception.ConflictException;
import com.dineflow.application.port.PasswordHasher;
import com.dineflow.application.port.TenantRepositoryPort;
import com.dineflow.application.port.TokenIssuer;
import com.dineflow.application.port.UserRepositoryPort;
import com.dineflow.domain.entity.Tenant;
import com.dineflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final AccessToken TOKEN = new AccessToken("jwt", 3600);

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private TenantRepositoryPort tenantRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenIssuer tokenIssuer;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, tenantRepository, passwordHasher, tokenIssuer);
    }

    @Test
    void registerCreatesTenantAndUserWithHashedPassword() {
        when(userRepository.existsByEmail("owner@example.com")).thenReturn(false);
        when(tenantRepository.saveTenant(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordHasher.hash("secret-pass")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenIssuer.issue(any(), any(), any())).thenReturn(TOKEN);

        AccessToken result = service.register("My Restaurant", "Owner", "  Owner@Example.com ", "secret-pass");

        assertEquals(TOKEN, result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("owner@example.com", captor.getValue().getEmail());
        assertEquals("HASH", captor.getValue().getPasswordHash());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("owner@example.com")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.register("My Restaurant", "Owner", "owner@example.com", "secret-pass"));

        verify(tenantRepository, never()).saveTenant(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = new User(7L, "Owner", "owner@example.com", "HASH");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret-pass", "HASH")).thenReturn(true);
        when(tokenIssuer.issue(null, 7L, "owner@example.com")).thenReturn(TOKEN);

        assertEquals(TOKEN, service.login("Owner@example.com", "secret-pass"));
    }

    @Test
    void loginFailsForWrongPassword() {
        User user = new User(7L, "Owner", "owner@example.com", "HASH");
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "HASH")).thenReturn(false);

        assertThrows(AuthenticationFailedException.class, () -> service.login("owner@example.com", "wrong"));
        verify(tokenIssuer, never()).issue(any(), any(), any());
    }

    @Test
    void loginFailsForUnknownEmailWithSameError() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        AuthenticationFailedException ex = assertThrows(AuthenticationFailedException.class,
                () -> service.login("nobody@example.com", "whatever"));

        assertEquals("Invalid email or password.", ex.getMessage());
    }
}
