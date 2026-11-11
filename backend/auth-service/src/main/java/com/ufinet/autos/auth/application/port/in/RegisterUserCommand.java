package com.ufinet.autos.auth.application.port.in;

/**
 * Command: datos de entrada del caso de uso de registro (independiente de HTTP).
 */
public record RegisterUserCommand(String fullName, String email, String rawPassword) {

    @Override
    public String toString() {
        return "RegisterUserCommand[fullName=" + fullName + ", email=" + email + ", rawPassword=****]";
    }
}
