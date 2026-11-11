package com.ufinet.autos.auth.infrastructure.adapter.out.security;

import com.ufinet.autos.auth.application.port.out.TokenProviderPort;
import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.security.AuthenticatedUser;
import com.ufinet.autos.auth.infrastructure.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Adaptador JWT (HS256). Emite tokens con:
 * <ul>
 *   <li>sub   = id del usuario</li>
 *   <li>email = correo</li>
 *   <li>name  = nombre completo</li>
 * </ul>
 * El car-service valida el token con la misma clave compartida.
 */
@Slf4j
@Component
public class JwtTokenProvider implements TokenProviderPort {

    static final String CLAIM_EMAIL = "email";
    static final String CLAIM_NAME = "name";

    private final JwtProperties properties;
    private final SecretKey key;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.clock = clock;
    }

    @Override
    public String generateToken(User user) {
        Instant now = clock.instant();
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_NAME, user.getFullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(getExpirationSeconds())))
                .signWith(key)
                .compact();
    }

    @Override
    public long getExpirationSeconds() {
        return properties.expirationMinutes() * 60;
    }

    /**
     * Valida firma, emisor y expiración. Devuelve vacío si el token no es válido.
     */
    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.issuer())
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_EMAIL, String.class),
                    claims.get(CLAIM_NAME, String.class)));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token JWT rechazado: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
