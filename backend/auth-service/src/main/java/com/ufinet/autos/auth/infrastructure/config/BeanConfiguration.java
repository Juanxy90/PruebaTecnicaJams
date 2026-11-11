package com.ufinet.autos.auth.infrastructure.config;

import com.ufinet.autos.auth.application.port.in.GetUserProfileUseCase;
import com.ufinet.autos.auth.application.port.in.LoginUseCase;
import com.ufinet.autos.auth.application.port.in.RegisterUserUseCase;
import com.ufinet.autos.auth.application.port.out.PasswordHasherPort;
import com.ufinet.autos.auth.application.port.out.TokenProviderPort;
import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.application.service.GetUserProfileService;
import com.ufinet.autos.auth.application.service.LoginService;
import com.ufinet.autos.auth.application.service.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition Root: aquí se "conectan" los puertos con sus adaptadores.
 * Los casos de uso son POJOs; Spring solo los instancia (Inyección de Dependencias).
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepository,
                                                   PasswordHasherPort passwordHasher,
                                                   TokenProviderPort tokenProvider,
                                                   Clock clock) {
        return new RegisterUserService(userRepository, passwordHasher, tokenProvider, clock);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepository,
                                     PasswordHasherPort passwordHasher,
                                     TokenProviderPort tokenProvider) {
        return new LoginService(userRepository, passwordHasher, tokenProvider);
    }

    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(UserRepositoryPort userRepository) {
        return new GetUserProfileService(userRepository);
    }
}
