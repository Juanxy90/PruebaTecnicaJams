package com.ufinet.autos.car.domain.exception;

/**
 * Excepción base del dominio (independiente de HTTP).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
