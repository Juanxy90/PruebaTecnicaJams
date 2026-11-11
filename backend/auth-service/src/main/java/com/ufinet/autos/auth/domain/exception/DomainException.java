package com.ufinet.autos.auth.domain.exception;

/**
 * Excepción base del dominio. El dominio no conoce HTTP: el adaptador web
 * (GlobalExceptionHandler) es quien traduce estas excepciones a códigos de estado.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
