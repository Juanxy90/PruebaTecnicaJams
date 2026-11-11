package com.ufinet.autos.auth.application.port.in;

/**
 * Puerto de entrada (driving port): registrar un nuevo usuario.
 */
public interface RegisterUserUseCase {

    AuthResult register(RegisterUserCommand command);
}
