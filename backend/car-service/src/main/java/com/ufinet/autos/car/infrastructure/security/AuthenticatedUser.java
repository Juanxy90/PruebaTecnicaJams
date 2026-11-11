package com.ufinet.autos.car.infrastructure.security;

/**
 * Principal de Spring Security construido a partir de los claims del JWT.
 */
public record AuthenticatedUser(Long id, String email, String fullName) {
}
