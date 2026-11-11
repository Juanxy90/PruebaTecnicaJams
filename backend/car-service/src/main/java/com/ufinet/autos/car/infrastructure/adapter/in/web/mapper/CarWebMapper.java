package com.ufinet.autos.car.infrastructure.adapter.in.web.mapper;

import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;
import com.ufinet.autos.car.infrastructure.adapter.in.web.dto.CarRequest;
import com.ufinet.autos.car.infrastructure.adapter.in.web.dto.CarResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de la capa web: DTO HTTP &lt;-&gt; modelo de dominio.
 */
@Component
public class CarWebMapper {

    public CarDetails toDetails(CarRequest request) {
        return new CarDetails(
                request.brand(),
                request.model(),
                request.year(),
                request.plateNumber(),
                request.color(),
                request.photoUrl());
    }

    public CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getPlateNumber(),
                car.getColor(),
                car.getPhotoUrl(),
                car.getCreatedAt(),
                car.getUpdatedAt());
    }
}
