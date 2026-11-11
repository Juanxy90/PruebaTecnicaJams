package com.ufinet.autos.car.support;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Genera JWT de prueba equivalentes a los que emite auth-service.
 */
public final class TestTokens {

    public static final String SECRET =
            "3AumwJ9Q1DMui4BH/cavAZDGdVQQ5uoPPY0fbE6lQZPxgii5rbRMCaw01Ormv72AkFHHktAQuuFYbGYik5LJXg==";
    public static final String ISSUER = "ufinet-autos-auth";

    private TestTokens() {
    }

    public static String validToken(long userId) {
        return token(userId, ISSUER, Instant.now(), Duration.ofHours(1));
    }

    public static String expiredToken(long userId) {
        return token(userId, ISSUER, Instant.now().minus(Duration.ofHours(2)), Duration.ofHours(1));
    }

    public static String token(long userId, String issuer, Instant issuedAt, Duration validity) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("email", "user" + userId + "@ufinet.com")
                .claim("name", "Usuario " + userId)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(validity)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();
    }
}
