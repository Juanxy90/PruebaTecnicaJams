package com.ufinet.autos.car.infrastructure.adapter.out.persistence.specification;

import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import com.ufinet.autos.car.infrastructure.adapter.out.persistence.entity.CarEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

/**
 * Patrón Specification: cada criterio de búsqueda es un predicado reutilizable
 * que se combina dinámicamente con AND según los filtros recibidos.
 */
public final class CarSpecifications {

    private static final char ESCAPE_CHAR = '\\';

    private CarSpecifications() {
    }

    public static Specification<CarEntity> fromCriteria(CarSearchCriteria criteria) {
        Specification<CarEntity> spec = ownedBy(criteria.ownerId());
        if (criteria.text() != null) {
            spec = spec.and(plateOrModelContains(criteria.text()));
        }
        if (criteria.brand() != null) {
            spec = spec.and(brandEquals(criteria.brand()));
        }
        if (criteria.year() != null) {
            spec = spec.and(yearEquals(criteria.year()));
        }
        return spec;
    }

    public static Specification<CarEntity> ownedBy(Long ownerId) {
        return (root, query, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    /** Búsqueda por placa o modelo (contiene, sin distinguir mayúsculas). */
    public static Specification<CarEntity> plateOrModelContains(String text) {
        String pattern = "%" + escapeLike(text.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("plateNumber")), pattern, ESCAPE_CHAR),
                cb.like(cb.lower(root.get("model")), pattern, ESCAPE_CHAR));
    }

    /** Filtro por marca (igualdad, sin distinguir mayúsculas). */
    public static Specification<CarEntity> brandEquals(String brand) {
        String value = brand.toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), value);
    }

    /** Filtro por año. */
    public static Specification<CarEntity> yearEquals(Integer year) {
        return (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    /** Escapa los comodines de LIKE (incluye '[' propio de SQL Server). */
    static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }
}
