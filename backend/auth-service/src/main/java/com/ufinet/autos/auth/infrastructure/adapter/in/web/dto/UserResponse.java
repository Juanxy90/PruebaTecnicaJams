package com.ufinet.autos.auth.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String fullName, String email, LocalDateTime createdAt) {
}
