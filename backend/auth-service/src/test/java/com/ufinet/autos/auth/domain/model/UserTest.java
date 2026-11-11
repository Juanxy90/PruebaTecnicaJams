package com.ufinet.autos.auth.domain.model;

import com.ufinet.autos.auth.domain.exception.InvalidUserDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Test
    @DisplayName("newUser normaliza el correo y recorta el nombre")
    void newUserNormalizesData() {
        User user = User.newUser("  Juan Pérez ", "  Juan.Perez@Correo.COM ", "hash", NOW);

        assertThat(user.getId()).isNull();
        assertThat(user.getFullName()).isEqualTo("Juan Pérez");
        assertThat(user.getEmail()).isEqualTo("juan.perez@correo.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getCreatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("newUser rechaza nombre vacío")
    void newUserRejectsBlankName(String name) {
        assertThatThrownBy(() -> User.newUser(name, "a@b.com", "hash", NOW))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin-arroba", "a@", "@b.com", "a@b"})
    @DisplayName("newUser rechaza correos inválidos")
    void newUserRejectsInvalidEmail(String email) {
        assertThatThrownBy(() -> User.newUser("Juan", email, "hash", NOW))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("newUser rechaza hash de contraseña vacío")
    void newUserRejectsBlankPassword() {
        assertThatThrownBy(() -> User.newUser("Juan", "a@b.com", " ", NOW))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    @DisplayName("toString no expone el hash de la contraseña")
    void toStringHidesPassword() {
        User user = User.newUser("Juan", "a@b.com", "super-secret-hash", NOW);
        assertThat(user.toString()).doesNotContain("super-secret-hash");
    }
}
