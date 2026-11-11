package com.ufinet.autos.car.infrastructure.adapter.out.persistence.mapper;

import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.infrastructure.adapter.out.persistence.entity.CarEntity;
import org.springframework.stereotype.Component;

/**
 * Data Mapper: dominio &lt;-&gt; entidad JPA.
 */
@Component
public class CarPersistenceMapper {

    public CarEntity toEntity(Car car) {
        return CarEntity.builder()
                .id(car.getId())
                .brand(car.getBrand())
                .model(car.getModel())
                .year(car.getYear())
                .plateNumber(car.getPlateNumber())
                .color(car.getColor())
                .photoUrl(car.getPhotoUrl())
                .ownerId(car.getOwnerId())
                .createdAt(car.getCreatedAt())
                .updatedAt(car.getUpdatedAt())
                .build();
    }

    public Car toDomain(CarEntity entity) {
        return Car.builder()
                .id(entity.getId())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .year(entity.getYear())
                .plateNumber(entity.getPlateNumber())
                .color(entity.getColor())
                .photoUrl(entity.getPhotoUrl())
                .ownerId(entity.getOwnerId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
