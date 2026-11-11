package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.in.GetCarUseCase;
import com.ufinet.autos.car.application.port.in.SearchCarsUseCase;
import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Casos de uso de solo lectura (separación comandos/consultas, estilo CQRS ligero).
 */
@RequiredArgsConstructor
public class CarQueryService implements GetCarUseCase, SearchCarsUseCase {

    private final CarRepositoryPort carRepository;

    @Override
    public Car getById(Long ownerId, Long carId) {
        return carRepository.findByIdAndOwnerId(carId, ownerId)
                .orElseThrow(() -> new CarNotFoundException(carId));
    }

    @Override
    public List<Car> search(CarSearchCriteria criteria) {
        return carRepository.search(criteria);
    }

    @Override
    public List<String> findBrands(Long ownerId) {
        return carRepository.findDistinctBrandsByOwnerId(ownerId);
    }
}
