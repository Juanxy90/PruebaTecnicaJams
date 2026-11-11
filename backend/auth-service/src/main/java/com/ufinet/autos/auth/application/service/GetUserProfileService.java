package com.ufinet.autos.auth.application.service;

import com.ufinet.autos.auth.application.port.in.GetUserProfileUseCase;
import com.ufinet.autos.auth.application.port.out.UserRepositoryPort;
import com.ufinet.autos.auth.domain.exception.UserNotFoundException;
import com.ufinet.autos.auth.domain.model.User;
import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: obtener el perfil de un usuario.
 */
@RequiredArgsConstructor
public class GetUserProfileService implements GetUserProfileUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
