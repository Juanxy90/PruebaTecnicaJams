package com.ufinet.autos.car.domain.exception;

public class PlateAlreadyRegisteredException extends DomainException {

    public PlateAlreadyRegisteredException(String plateNumber) {
        super("Ya existe un auto registrado con la placa " + plateNumber);
    }
}
