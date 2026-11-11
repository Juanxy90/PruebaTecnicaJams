package com.ufinet.autos.auth.infrastructure.adapter.out.persistence;

import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.EmailAlreadyRegisteredException;
import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.ufinet.autos.auth.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida (Adapter): implementa el puerto de persistencia del dominio
 * usando Spring Data JPA sobre SQL Server.
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public User save(User user) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(user)));
        } catch (DataIntegrityViolationException ex) {
            // Condición de carrera: dos registros simultáneos con el mismo correo (UNIQUE en BD)
            throw new EmailAlreadyRegisteredException(user.getEmail());
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
