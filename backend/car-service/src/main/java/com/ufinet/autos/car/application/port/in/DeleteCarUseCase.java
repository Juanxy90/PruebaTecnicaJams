package com.ufinet.autos.car.application.port.in;

/**
 * Puerto de entrada: eliminar un auto del usuario autenticado.
 */
public interface DeleteCarUseCase {

    void delete(Long ownerId, Long carId);
}
