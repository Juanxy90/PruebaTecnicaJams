package com.ufinet.autos.car.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o editar un auto")
public record CarRequest(

        @Schema(example = "Toyota")
        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca debe tener máximo 50 caracteres")
        String brand,

        @Schema(example = "Corolla")
        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 50, message = "El modelo debe tener máximo 50 caracteres")
        String model,

        @Schema(example = "2022")
        @NotNull(message = "El año es obligatorio")
        @Min(value = 1886, message = "El año debe ser mayor o igual a 1886")
        @Max(value = 2100, message = "El año no es válido")
        Integer year,

        @Schema(example = "ABC-123")
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9 -]{5,12}$", message = "La placa solo admite letras, números y '-' (5 a 10 caracteres)")
        String plateNumber,

        @Schema(example = "Rojo")
        @NotBlank(message = "El color es obligatorio")
        @Size(max = 30, message = "El color debe tener máximo 30 caracteres")
        String color,

        @Schema(description = "URL simulada de la foto (la subida no es funcional)",
                example = "/uploads/cars/corolla.jpg")
        @Size(max = 500, message = "La URL de la foto debe tener máximo 500 caracteres")
        String photoUrl
) {
}
