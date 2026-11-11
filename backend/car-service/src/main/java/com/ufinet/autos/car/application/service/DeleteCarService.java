package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.in.DeleteCarUseCase;
import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.model.Car;
import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: eliminar auto (solo si pertenece al usuario).
 */
@RequiredArgsConstructor
public class DeleteCarService implements DeleteCarUseCase {

    private final CarRepositoryPort carRepository;

    @Override
    public void delete(Long ownerId, Long carId) {
        Car car = carRepository.findByIdAndOwnerId(carId, ownerId)
                .orElseThrow(() -> new CarNotFoundException(carId));
        carRepository.deleteById(car.getId());
    }
}
