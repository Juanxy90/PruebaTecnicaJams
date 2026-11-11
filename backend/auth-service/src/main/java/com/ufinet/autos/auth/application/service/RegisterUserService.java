package com.ufinet.autos.auth.application.service;

import com.ufinet.autos.auth.application.port.in.AuthResult;
import com.ufinet.autos.auth.application.port.in.RegisterUserCommand;
import com.ufinet.autos.auth.application.port.in.RegisterUserUseCase;
import com.ufinet.autos.auth.application.port.out.PasswordHasherPort;
import com.ufinet.autos.auth.application.port.out.TokenProviderPort;
import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.EmailAlreadyRegisteredException;
import com.ufinet.autos.auth.domain.model.User;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Caso de uso: registro de usuario.
 * Clase sin anotaciones de Spring: el núcleo no depende del framework y se
 * registra como bean en {@code BeanConfiguration}.
 */
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;
    private final Clock clock;

    @Override
    public AuthResult register(RegisterUserCommand command) {
        String email = User.normalizeEmail(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User newUser = User.newUser(
                command.fullName(),
                email,
                passwordHasher.hash(command.rawPassword()),
                LocalDateTime.now(clock));

        User saved = userRepository.save(newUser);
        return new AuthResult(tokenProvider.generateToken(saved), tokenProvider.getExpirationSeconds(), saved);
    }
}
