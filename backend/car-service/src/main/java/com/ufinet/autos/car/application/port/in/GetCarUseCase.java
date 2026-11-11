package com.ufinet.autos.car.application.port.in;

import com.ufinet.autos.car.domain.model.Car;

/**
 * Puerto de entrada: obtener un auto por id (solo si pertenece al usuario).
 */
public interface GetCarUseCase {

    Car getById(Long ownerId, Long carId);
}
