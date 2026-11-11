package com.ufinet.autos.auth.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Correo o contraseña incorrectos");
    }
}
