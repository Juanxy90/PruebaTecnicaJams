package com.ufinet.autos.auth.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación con el JWT emitido")
public record AuthResponse(
        String accessToken,
        @Schema(example = "Bearer") String tokenType,
        @Schema(description = "Segundos de vigencia del token", example = "3600") long expiresIn,
        UserResponse user
) {
}
