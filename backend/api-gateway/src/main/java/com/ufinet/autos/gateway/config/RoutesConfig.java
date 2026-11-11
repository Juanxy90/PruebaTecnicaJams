package com.ufinet.autos.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Patrón API Gateway: enruta cada prefijo de la API a su microservicio.
 * <pre>
 *   /api/auth/**  -> auth-service
 *   /api/users/** -> auth-service
 *   /api/cars/**  -> car-service
 * </pre>
 */
@Configuration
public class RoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, GatewayProperties properties) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .uri(properties.authServiceUrl()))
                .route("car-service", r -> r
                        .path("/api/cars", "/api/cars/**")
                        .uri(properties.carServiceUrl()))
                .build();
    }
}
