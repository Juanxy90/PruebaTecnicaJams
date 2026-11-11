package com.ufinet.autos.auth.application.service;

import com.ufinet.autos.auth.application.port.in.AuthResult;
import com.ufinet.autos.auth.application.port.in.LoginCommand;
import com.ufinet.autos.auth.application.port.in.LoginUseCase;
import com.ufinet.autos.auth.application.port.out.PasswordHasherPort;
import com.ufinet.autos.auth.application.port.out.TokenProviderPort;
import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.InvalidCredentialsException;
import com.ufinet.autos.auth.domain.model.User;
import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: login. Devuelve el mismo error si el correo no existe o si la
 * contraseña es incorrecta, para no revelar qué cuentas existen.
 */
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    @Override
    public AuthResult login(LoginCommand command) {
        User user = userRepository.findByEmail(User.normalizeEmail(command.email()))
                .filter(u -> passwordHasher.matches(command.rawPassword(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        return new AuthResult(tokenProvider.generateToken(user), tokenProvider.getExpirationSeconds(), user);
    }
}
