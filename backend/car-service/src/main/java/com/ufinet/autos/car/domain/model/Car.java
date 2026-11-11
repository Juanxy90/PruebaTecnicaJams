package com.ufinet.autos.car.domain.model;

import com.ufinet.autos.car.domain.exception.InvalidCarDataException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad de dominio "Auto" (agregado raíz). POJO inmutable y sin dependencias de framework.
 * Contiene las reglas de negocio: año válido, pertenencia a un usuario, etc.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class Car {

    /** Año del primer automóvil patentado (Benz Patent-Motorwagen). */
    public static final int MIN_YEAR = 1886;

    private final Long id;
    private final String brand;
    private final String model;
    private final Integer year;
    private final String plateNumber;
    private final String color;
    private final String photoUrl;
    private final Long ownerId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Factory Method: crea un auto nuevo para el usuario indicado.
     */
    public static Car create(CarDetails details, Long ownerId, LocalDateTime now) {
        if (ownerId == null) {
            throw new InvalidCarDataException("El auto debe pertenecer a un usuario");
        }
        validateYear(details.year(), now);

        return Car.builder()
                .brand(details.brand())
                .model(details.model())
                .year(details.year())
                .plateNumber(details.plateNumber())
                .color(details.color())
                .photoUrl(details.photoUrl())
                .ownerId(ownerId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Devuelve una nueva instancia con los datos actualizados (inmutabilidad).
     */
    public Car updateWith(CarDetails details, LocalDateTime now) {
        validateYear(details.year(), now);

        return toBuilder()
                .brand(details.brand())
                .model(details.model())
                .year(details.year())
                .plateNumber(details.plateNumber())
                .color(details.color())
                .photoUrl(details.photoUrl())
                .updatedAt(now)
                .build();
    }

    public boolean belongsTo(Long userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    private static void validateYear(int year, LocalDateTime now) {
        int maxYear = now.getYear() + 1;
        if (year < MIN_YEAR || year > maxYear) {
            throw new InvalidCarDataException(
                    "El año debe estar entre " + MIN_YEAR + " y " + maxYear);
        }
    }
}
