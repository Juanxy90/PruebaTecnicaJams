package com.ufinet.autos.auth.infrastructure.adapter.in.web.mapper;

import com.ufinet.autos.auth.application.port.in.AuthResult;
import com.ufinet.autos.auth.application.port.in.LoginCommand;
import com.ufinet.autos.auth.application.port.in.RegisterUserCommand;
import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.AuthResponse;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de la capa web: DTO HTTP &lt;-&gt; comandos/modelos de la aplicación.
 */
@Component
public class AuthWebMapper {

    private static final String TOKEN_TYPE = "Bearer";

    public RegisterUserCommand toCommand(RegisterRequest request) {
        return new RegisterUserCommand(request.fullName(), request.email(), request.password());
    }

    public LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    public AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.accessToken(), TOKEN_TYPE, result.expiresInSeconds(), toResponse(result.user()));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getCreatedAt());
    }
}
