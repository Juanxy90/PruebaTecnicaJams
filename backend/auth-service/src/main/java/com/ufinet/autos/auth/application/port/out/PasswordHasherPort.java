package com.ufinet.autos.auth.application.port.out;

/**
 * Puerto de salida para el hash de contraseñas.
 * Strategy: la implementación (BCrypt, Argon2, ...) es intercambiable.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
