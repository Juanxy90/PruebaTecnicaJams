package com.ufinet.autos.auth.infrastructure.adapter.out.security;

import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.security.AuthenticatedUser;
import com.ufinet.autos.auth.infrastructure.security.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "3AumwJ9Q1DMui4BH/cavAZDGdVQQ5uoPPY0fbE6lQZPxgii5rbRMCaw01Ormv72AkFHHktAQuuFYbGYik5LJXg==";
    private static final String OTHER_SECRET =
            "bXktb3RoZXItc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3Nlcy0xMjM0NTY3ODk=";
    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    private final JwtProperties properties = new JwtProperties(SECRET, 60, "ufinet-autos-auth");
    private final JwtTokenProvider provider = new JwtTokenProvider(properties, Clock.fixed(NOW, ZoneOffset.UTC));
    private final User user = User.builder().id(5L).fullName("Demo User").email("demo@ufinet.com").build();

    @Test
    @DisplayName("Genera un token que se puede validar y contiene los claims del usuario")
    void generateAndParse() {
        String token = provider.generateToken(user);

        Optional<AuthenticatedUser> parsed = provider.parse(token);

        assertThat(parsed).contains(new AuthenticatedUser(5L, "demo@ufinet.com", "Demo User"));
        assertThat(provider.getExpirationSeconds()).isEqualTo(3600);
    }

    @Test
    @DisplayName("Rechaza un token expirado")
    void rejectsExpiredToken() {
        String token = provider.generateToken(user);
        JwtTokenProvider later = new JwtTokenProvider(properties,
                Clock.fixed(NOW.plus(Duration.ofHours(2)), ZoneOffset.UTC));

        assertThat(later.parse(token)).isEmpty();
    }

    @Test
    @DisplayName("Rechaza un token firmado con otra clave")
    void rejectsTokenSignedWithOtherKey() {
        JwtTokenProvider other = new JwtTokenProvider(
                new JwtProperties(OTHER_SECRET, 60, "ufinet-autos-auth"), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(provider.parse(other.generateToken(user))).isEmpty();
    }

    @Test
    @DisplayName("Rechaza un token con otro emisor")
    void rejectsTokenWithOtherIssuer() {
        JwtTokenProvider other = new JwtTokenProvider(
                new JwtProperties(SECRET, 60, "otro-emisor"), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(provider.parse(other.generateToken(user))).isEmpty();
    }

    @Test
    @DisplayName("Rechaza basura")
    void rejectsGarbage() {
        assertThat(provider.parse("no-es-un-jwt")).isEmpty();
        assertThat(provider.parse("")).isEmpty();
    }
}
