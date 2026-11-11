package com.ufinet.autos.car.domain.model;

import com.ufinet.autos.car.domain.exception.InvalidCarDataException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value Object con los datos editables de un auto. Se auto-valida y normaliza
 * en su construcción, por lo que nunca existe una instancia inválida.
 */
public record CarDetails(String brand,
                         String model,
                         Integer year,
                         String plateNumber,
                         String color,
                         String photoUrl) {

    public static final int MAX_BRAND_LENGTH = 50;
    public static final int MAX_MODEL_LENGTH = 50;
    public static final int MAX_COLOR_LENGTH = 30;
    public static final int MAX_PHOTO_URL_LENGTH = 500;
    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z0-9-]{5,10}$");

    public CarDetails {
        brand = requireText(brand, "marca", MAX_BRAND_LENGTH);
        model = requireText(model, "modelo", MAX_MODEL_LENGTH);
        color = requireText(color, "color", MAX_COLOR_LENGTH);

        if (year == null) {
            throw new InvalidCarDataException("El año es obligatorio");
        }

        plateNumber = normalizePlate(plateNumber);
        if (plateNumber == null || !PLATE_PATTERN.matcher(plateNumber).matches()) {
            throw new InvalidCarDataException(
                    "La placa debe tener entre 5 y 10 caracteres alfanuméricos (se permite '-')");
        }

        photoUrl = (photoUrl == null || photoUrl.isBlank()) ? null : photoUrl.trim();
        if (photoUrl != null && photoUrl.length() > MAX_PHOTO_URL_LENGTH) {
            throw new InvalidCarDataException("La URL de la foto es demasiado larga");
        }
    }

    /**
     * Normaliza una placa: sin espacios y en mayúsculas ("abc 123" -> "ABC123").
     */
    public static String normalizePlate(String plate) {
        return plate == null ? null : plate.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidCarDataException("El campo " + field + " es obligatorio");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new InvalidCarDataException("El campo " + field + " debe tener máximo " + maxLength + " caracteres");
        }
        return trimmed;
    }
}
