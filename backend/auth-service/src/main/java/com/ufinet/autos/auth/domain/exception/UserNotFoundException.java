package com.ufinet.autos.auth.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long id) {
        super("No se encontró el usuario con id " + id);
    }
}
