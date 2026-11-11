package com.ufinet.autos.car.infrastructure.security;

import com.ufinet.autos.car.support.TestTokens;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenValidatorTest {

    private final JwtTokenValidator validator = new JwtTokenValidator(
            new JwtProperties(TestTokens.SECRET, TestTokens.ISSUER), Clock.systemUTC());

    @Test
    void acceptsValidToken() {
        assertThat(validator.validate(TestTokens.validToken(7L)))
                .contains(new AuthenticatedUser(7L, "user7@ufinet.com", "Usuario 7"));
    }

    @Test
    void rejectsExpiredToken() {
        assertThat(validator.validate(TestTokens.expiredToken(7L))).isEmpty();
    }

    @Test
    void rejectsWrongIssuer() {
        String token = TestTokens.token(7L, "otro", Instant.now(), Duration.ofHours(1));
        assertThat(validator.validate(token)).isEmpty();
    }

    @Test
    void rejectsTamperedToken() {
        String token = TestTokens.validToken(7L);
        String tampered = token.substring(0, token.length() - 4) + "abcd";
        assertThat(validator.validate(tampered)).isEmpty();
    }
}
