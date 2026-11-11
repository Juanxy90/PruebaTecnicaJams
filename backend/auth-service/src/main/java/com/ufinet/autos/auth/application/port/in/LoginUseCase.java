package com.ufinet.autos.auth.application.port.in;

/**
 * Puerto de entrada (driving port): autenticar un usuario y emitir un JWT.
 */
public interface LoginUseCase {

    AuthResult login(LoginCommand command);
}
