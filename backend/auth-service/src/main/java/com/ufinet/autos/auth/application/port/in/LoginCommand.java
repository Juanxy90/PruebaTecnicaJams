package com.ufinet.autos.auth.application.port.in;

/**
 * Command: credenciales de login (independiente de HTTP).
 */
public record LoginCommand(String email, String rawPassword) {

    @Override
    public String toString() {
        return "LoginCommand[email=" + email + ", rawPassword=****]";
    }
}
