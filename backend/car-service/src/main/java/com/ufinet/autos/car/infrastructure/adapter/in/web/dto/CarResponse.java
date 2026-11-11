package com.ufinet.autos.car.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

public record CarResponse(
        Long id,
        String brand,
        String model,
        Integer year,
        String plateNumber,
        String color,
        String photoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
