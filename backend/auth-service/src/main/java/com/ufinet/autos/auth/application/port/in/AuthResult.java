package com.ufinet.autos.auth.application.port.in;

import com.ufinet.autos.auth.domain.model.User;

/**
 * Resultado de una autenticación exitosa: token emitido + usuario.
 */
public record AuthResult(String accessToken, long expiresInSeconds, User user) {
}
