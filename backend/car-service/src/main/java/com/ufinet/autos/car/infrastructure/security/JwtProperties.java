package com.ufinet.autos.car.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración tipada del JWT (prefijo {@code security.jwt}).
 * Debe compartir secreto y emisor con auth-service.
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, String issuer) {
}
