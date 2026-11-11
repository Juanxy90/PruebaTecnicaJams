package com.ufinet.autos.auth.infrastructure.adapter.in.web;

import com.ufinet.autos.auth.application.port.in.LoginUseCase;
import com.ufinet.autos.auth.application.port.in.RegisterUserUseCase;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.AuthResponse;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ufinet.autos.auth.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para registro y login (endpoints públicos).
 * Depende solo de los puertos de entrada (interfaces), nunca de las implementaciones.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro de usuarios y login con JWT")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final AuthWebMapper mapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario", description = "Crea el usuario y devuelve un JWT listo para usar")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return mapper.toResponse(registerUserUseCase.register(mapper.toCommand(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve un JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.toResponse(loginUseCase.login(mapper.toCommand(request)));
    }
}
