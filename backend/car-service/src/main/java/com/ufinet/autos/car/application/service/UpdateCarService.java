package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.in.UpdateCarUseCase;
import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.exception.PlateAlreadyRegisteredException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Caso de uso: editar auto. Un usuario solo puede editar sus propios autos;
 * si el auto es de otro usuario se responde "no encontrado" para no filtrar información.
 */
@RequiredArgsConstructor
public class UpdateCarService implements UpdateCarUseCase {

    private final CarRepositoryPort carRepository;
    private final Clock clock;

    @Override
    public Car update(Long ownerId, Long carId, CarDetails details) {
        Car current = carRepository.findByIdAndOwnerId(carId, ownerId)
                .orElseThrow(() -> new CarNotFoundException(carId));

        if (carRepository.existsByPlateNumberAndIdNot(details.plateNumber(), carId)) {
            throw new PlateAlreadyRegisteredException(details.plateNumber());
        }

        return carRepository.save(current.updateWith(details, LocalDateTime.now(clock)));
    }
}
