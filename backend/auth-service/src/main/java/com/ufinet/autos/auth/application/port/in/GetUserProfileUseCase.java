package com.ufinet.autos.auth.application.port.in;

import com.ufinet.autos.auth.domain.model.User;

/**
 * Puerto de entrada (driving port): consultar el perfil del usuario autenticado.
 */
public interface GetUserProfileUseCase {

    User getById(Long userId);
}
