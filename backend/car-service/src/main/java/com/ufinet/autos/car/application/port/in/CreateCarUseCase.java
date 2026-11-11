package com.ufinet.autos.car.application.port.in;

import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;

/**
 * Puerto de entrada: registrar un auto para el usuario autenticado.
 */
public interface CreateCarUseCase {

    Car create(Long ownerId, CarDetails details);
}
