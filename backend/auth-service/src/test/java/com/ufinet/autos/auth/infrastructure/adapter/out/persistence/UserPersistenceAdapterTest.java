package com.ufinet.autos.auth.infrastructure.adapter.out.persistence;

import com.ufinet.autos.auth.domain.exception.EmailAlreadyRegisteredException;
import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba del adaptador de persistencia contra una BD en memoria (H2).
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({UserPersistenceAdapter.class, UserPersistenceMapper.class})
class UserPersistenceAdapterTest {

    @Autowired
    private UserPersistenceAdapter adapter;

    private User newUser(String email) {
        return User.newUser("Demo", email, "hash", LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("Guarda y recupera un usuario por correo e id")
    void saveAndFind() {
        User saved = adapter.save(newUser("demo@ufinet.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(adapter.existsByEmail("demo@ufinet.com")).isTrue();
        assertThat(adapter.findByEmail("demo@ufinet.com")).map(User::getId).contains(saved.getId());
        assertThat(adapter.findById(saved.getId())).map(User::getEmail).contains("demo@ufinet.com");
    }

    @Test
    @DisplayName("Traduce la violación de UNIQUE a EmailAlreadyRegisteredException")
    void duplicatedEmail() {
        adapter.save(newUser("dup@ufinet.com"));

        assertThatThrownBy(() -> adapter.save(newUser("dup@ufinet.com")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
