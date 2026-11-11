package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.in.CreateCarUseCase;
import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.PlateAlreadyRegisteredException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Caso de uso: crear auto. La placa es única en todo el sistema.
 */
@RequiredArgsConstructor
public class CreateCarService implements CreateCarUseCase {

    private final CarRepositoryPort carRepository;
    private final Clock clock;

    @Override
    public Car create(Long ownerId, CarDetails details) {
        if (carRepository.existsByPlateNumber(details.plateNumber())) {
            throw new PlateAlreadyRegisteredException(details.plateNumber());
        }
        return carRepository.save(Car.create(details, ownerId, LocalDateTime.now(clock)));
    }
}
