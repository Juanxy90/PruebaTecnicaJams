package com.ufinet.autos.car.domain.model;

/**
 * Value Object con los criterios de búsqueda/filtrado de autos.
 *
 * @param ownerId usuario dueño de los autos (obligatorio: un usuario solo ve sus autos)
 * @param text    texto libre que se busca en la placa o en el modelo
 * @param brand   filtro exacto por marca (sin distinguir mayúsculas)
 * @param year    filtro exacto por año
 */
public record CarSearchCriteria(Long ownerId, String text, String brand, Integer year) {

    public CarSearchCriteria {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId es obligatorio");
        }
        text = blankToNull(text);
        brand = blankToNull(brand);
    }

    public static CarSearchCriteria forOwner(Long ownerId) {
        return new CarSearchCriteria(ownerId, null, null, null);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
