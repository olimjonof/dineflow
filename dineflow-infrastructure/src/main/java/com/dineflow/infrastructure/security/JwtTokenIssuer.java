package com.dineflow.infrastructure.security;

import com.dineflow.application.dto.AccessToken;
import com.dineflow.application.port.TokenIssuer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    public static final String TENANT_CLAIM = "tenant_id";
    public static final String ISSUER = "dineflow";

    private final JwtEncoder encoder;
    private final Duration lifetime;

    public JwtTokenIssuer(
            JwtEncoder encoder,
            @Value("${dineflow.security.jwt.expiration-minutes:60}") long expirationMinutes
    ) {
        this.encoder = encoder;
        this.lifetime = Duration.ofMinutes(expirationMinutes);
    }

    @Override
    public AccessToken issue(Long userId, Long tenantId, String email) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim(TENANT_CLAIM, tenantId)
                .claim("email", email)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AccessToken(token, lifetime.toSeconds());
    }
}
