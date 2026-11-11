package com.ufinet.autos.auth.domain.exception;

public class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(String email) {
        super("Ya existe un usuario registrado con el correo " + email);
    }
}
