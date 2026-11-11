package com.ufinet.autos.car.application.port.in;

import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;

import java.util.List;

/**
 * Puerto de entrada: listar / buscar (placa o modelo) / filtrar (marca o año) autos.
 */
public interface SearchCarsUseCase {

    List<Car> search(CarSearchCriteria criteria);

    List<String> findBrands(Long ownerId);
}
