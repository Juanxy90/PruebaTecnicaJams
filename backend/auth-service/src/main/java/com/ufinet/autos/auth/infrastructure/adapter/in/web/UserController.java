package com.ufinet.autos.auth.infrastructure.adapter.in.web;

import com.ufinet.autos.auth.application.port.in.GetUserProfileUseCase;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.UserResponse;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import com.ufinet.autos.auth.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para el perfil del usuario (requiere JWT).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final AuthWebMapper mapper;

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return mapper.toResponse(getUserProfileUseCase.getById(principal.id()));
    }
}
