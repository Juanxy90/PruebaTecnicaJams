package com.ufinet.autos.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * URLs de los microservicios y orígenes permitidos para CORS.
 */
@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(String authServiceUrl, String carServiceUrl, List<String> allowedOrigins) {
}
