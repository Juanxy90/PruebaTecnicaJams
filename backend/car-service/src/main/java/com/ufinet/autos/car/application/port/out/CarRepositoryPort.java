package com.ufinet.autos.car.application.port.out;

import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida de persistencia de autos (Repository).
 */
public interface CarRepositoryPort {

    Car save(Car car);

    Optional<Car> findByIdAndOwnerId(Long id, Long ownerId);

    List<Car> search(CarSearchCriteria criteria);

    List<String> findDistinctBrandsByOwnerId(Long ownerId);

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndIdNot(String plateNumber, Long id);

    void deleteById(Long id);
}
