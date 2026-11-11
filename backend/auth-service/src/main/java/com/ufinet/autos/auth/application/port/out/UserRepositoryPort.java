package com.ufinet.autos.auth.application.port.out;

import com.ufinet.autos.auth.domain.model.User;

import java.util.Optional;

/**
 * Puerto de salida (driven port) de persistencia. La aplicación depende de esta
 * abstracción y no de JPA (Principio de Inversión de Dependencias).
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);
}
