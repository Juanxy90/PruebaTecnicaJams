package com.ufinet.autos.auth.application.service;

import com.ufinet.autos.auth.application.port.in.AuthResult;
import com.ufinet.autos.auth.application.port.in.RegisterUserCommand;
import com.ufinet.autos.auth.application.port.out.PasswordHasherPort;
import com.ufinet.autos.auth.application.port.out.TokenProviderPort;
import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.EmailAlreadyRegisteredException;
import com.ufinet.autos.auth.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordHasher, tokenProvider, CLOCK);
    }

    @Test
    @DisplayName("Registra el usuario con la contraseña hasheada y devuelve un token")
    void registersUser() {
        when(userRepository.existsByEmail("juan@correo.com")).thenReturn(false);
        when(passwordHasher.hash("Password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.<User>getArgument(0).toBuilder().id(7L).build());
        when(tokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");
        when(tokenProvider.getExpirationSeconds()).thenReturn(3600L);

        AuthResult result = service.register(new RegisterUserCommand("Juan", "Juan@Correo.com", "Password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("juan@correo.com");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.expiresInSeconds()).isEqualTo(3600L);
        assertThat(result.user().getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("Lanza EmailAlreadyRegisteredException si el correo ya existe")
    void rejectsDuplicatedEmail() {
        when(userRepository.existsByEmail("juan@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterUserCommand("Juan", "juan@correo.com", "Password123")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
        verify(passwordHasher, never()).hash(any());
    }
}
