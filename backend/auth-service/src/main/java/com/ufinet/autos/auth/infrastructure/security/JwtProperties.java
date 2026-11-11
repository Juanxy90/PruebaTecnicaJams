package com.ufinet.autos.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración tipada del JWT (prefijo {@code security.jwt} en application.yml).
 *
 * @param secret            clave HMAC en Base64 (mínimo 256 bits)
 * @param expirationMinutes minutos de vigencia del token
 * @param issuer            emisor esperado del token
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, long expirationMinutes, String issuer) {
}
