package com.ufinet.autos.auth.infrastructure.adapter.out.persistence.mapper;

import com.ufinet.autos.auth.domain.model.User;
import com.ufinet.autos.auth.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper (Data Mapper): traduce entre el modelo de dominio y la entidad JPA.
 */
@Component
public class UserPersistenceMapper {

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
