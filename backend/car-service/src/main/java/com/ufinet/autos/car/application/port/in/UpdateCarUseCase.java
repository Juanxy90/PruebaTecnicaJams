package com.ufinet.autos.car.application.port.in;

import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;

/**
 * Puerto de entrada: editar un auto del usuario autenticado.
 */
public interface UpdateCarUseCase {

    Car update(Long ownerId, Long carId, CarDetails details);
}
