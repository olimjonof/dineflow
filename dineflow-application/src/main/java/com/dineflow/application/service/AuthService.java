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
import com.dineflow.domain.enums.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password.";

    private final UserRepositoryPort userRepository;
    private final TenantRepositoryPort tenantRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthService(
            UserRepositoryPort userRepository,
            TenantRepositoryPort tenantRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    /** Creates a new tenant together with its first user and returns a token for that user. */
    @Transactional
    public AccessToken register(String tenantName, String name, String email, String rawPassword) {
        String normalizedEmail = normalize(email);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered.");
        }

        Tenant tenant = tenantRepository.saveTenant(new Tenant(tenantName, TenantStatus.ACTIVE));
        User user = userRepository.save(
                new User(tenant.getId(), name, normalizedEmail, passwordHasher.hash(rawPassword))
        );

        return tokenIssuer.issue(user.getId(), user.getTenantId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AccessToken login(String email, String rawPassword) {
        User user = userRepository.findByEmail(normalize(email))
                .filter(found -> passwordHasher.matches(rawPassword, found.getPasswordHash()))
                .orElseThrow(() -> new AuthenticationFailedException(INVALID_CREDENTIALS));

        return tokenIssuer.issue(user.getId(), user.getTenantId(), user.getEmail());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
