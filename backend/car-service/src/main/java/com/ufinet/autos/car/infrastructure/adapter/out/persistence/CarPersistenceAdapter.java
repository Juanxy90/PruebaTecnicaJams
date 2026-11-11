package com.ufinet.autos.car.infrastructure.adapter.out.persistence;

import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.PlateAlreadyRegisteredException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import com.ufinet.autos.car.infrastructure.adapter.out.persistence.mapper.CarPersistenceMapper;
import com.ufinet.autos.car.infrastructure.adapter.out.persistence.repository.SpringDataCarRepository;
import com.ufinet.autos.car.infrastructure.adapter.out.persistence.specification.CarSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto de persistencia con Spring Data JPA (SQL Server).
 */
@Component
@RequiredArgsConstructor
public class CarPersistenceAdapter implements CarRepositoryPort {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final SpringDataCarRepository repository;
    private final CarPersistenceMapper mapper;

    @Override
    public Car save(Car car) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(car)));
        } catch (DataIntegrityViolationException ex) {
            // Condición de carrera sobre el índice UNIQUE de la placa
            throw new PlateAlreadyRegisteredException(car.getPlateNumber());
        }
    }

    @Override
    public Optional<Car> findByIdAndOwnerId(Long id, Long ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId).map(mapper::toDomain);
    }

    @Override
    public List<Car> search(CarSearchCriteria criteria) {
        return repository.findAll(CarSpecifications.fromCriteria(criteria), DEFAULT_SORT)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<String> findDistinctBrandsByOwnerId(Long ownerId) {
        return repository.findDistinctBrandsByOwnerId(ownerId);
    }

    @Override
    public boolean existsByPlateNumber(String plateNumber) {
        return repository.existsByPlateNumber(plateNumber);
    }

    @Override
    public boolean existsByPlateNumberAndIdNot(String plateNumber, Long id) {
        return repository.existsByPlateNumberAndIdNot(plateNumber, id);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
