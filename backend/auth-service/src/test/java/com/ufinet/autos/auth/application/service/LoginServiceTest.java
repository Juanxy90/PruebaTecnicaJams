package com.ufinet.autos.auth.application.service;

import com.ufinet.autos.auth.application.port.in.AuthResult;
import com.ufinet.autos.auth.application.port.in.LoginCommand;
import com.ufinet.autos.auth.application.port.out.PasswordHasherPort;
import com.ufinet.autos.auth.application.port.out.TokenProviderPort;
import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.InvalidCredentialsException;
import com.ufinet.autos.auth.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    @InjectMocks
    private LoginService service;

    private final User user = User.builder().id(1L).fullName("Demo").email("demo@ufinet.com").passwordHash("hashed").build();

    @Test
    @DisplayName("Devuelve token cuando las credenciales son correctas")
    void loginSuccess() {
        when(userRepository.findByEmail("demo@ufinet.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Password123", "hashed")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("jwt");
        when(tokenProvider.getExpirationSeconds()).thenReturn(3600L);

        AuthResult result = service.login(new LoginCommand(" DEMO@ufinet.com ", "Password123"));

        assertThat(result.accessToken()).isEqualTo("jwt");
        assertThat(result.user()).isEqualTo(user);
    }

    @Test
    @DisplayName("Falla con InvalidCredentialsException si la contraseña es incorrecta")
    void loginWrongPassword() {
        when(userRepository.findByEmail("demo@ufinet.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("bad", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginCommand("demo@ufinet.com", "bad")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Falla con el mismo error si el usuario no existe (no revela cuentas)")
    void loginUnknownUser() {
        when(userRepository.findByEmail("nadie@ufinet.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginCommand("nadie@ufinet.com", "Password123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(passwordHasher, never()).matches(any(), any());
    }
}
