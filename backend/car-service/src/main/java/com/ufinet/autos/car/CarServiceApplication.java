package com.ufinet.autos.car;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de entrada del microservicio de autos.
 * Valida los JWT emitidos por auth-service (clave compartida); no maneja contraseñas.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CarServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarServiceApplication.class, args);
    }
}
