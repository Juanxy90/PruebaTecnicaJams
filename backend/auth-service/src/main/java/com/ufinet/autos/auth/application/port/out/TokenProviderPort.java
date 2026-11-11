package com.ufinet.autos.auth.application.port.out;

import com.ufinet.autos.auth.domain.model.User;

/**
 * Puerto de salida para la emisión de tokens de acceso (JWT u otro).
 */
public interface TokenProviderPort {

    String generateToken(User user);

    long getExpirationSeconds();
}
