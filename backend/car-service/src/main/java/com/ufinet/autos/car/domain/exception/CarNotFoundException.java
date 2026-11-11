package com.ufinet.autos.car.domain.exception;

public class CarNotFoundException extends DomainException {

    public CarNotFoundException(Long carId) {
        super("No se encontró el auto con id " + carId);
    }
}
