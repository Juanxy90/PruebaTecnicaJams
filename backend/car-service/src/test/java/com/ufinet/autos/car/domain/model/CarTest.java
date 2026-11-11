package com.ufinet.autos.car.domain.model;

import com.ufinet.autos.car.domain.exception.InvalidCarDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 12, 0);
    private final CarDetails details = new CarDetails("Toyota", "Corolla", 2022, "ABC-123", "Rojo", null);

    @Test
    @DisplayName("create asigna dueño y fechas")
    void createAssignsOwnerAndDates() {
        Car car = Car.create(details, 10L, NOW);

        assertThat(car.getId()).isNull();
        assertThat(car.getOwnerId()).isEqualTo(10L);
        assertThat(car.getPlateNumber()).isEqualTo("ABC-123");
        assertThat(car.getCreatedAt()).isEqualTo(NOW);
        assertThat(car.getUpdatedAt()).isEqualTo(NOW);
        assertThat(car.belongsTo(10L)).isTrue();
        assertThat(car.belongsTo(11L)).isFalse();
    }

    @Test
    @DisplayName("create exige un dueño")
    void createRequiresOwner() {
        assertThatThrownBy(() -> Car.create(details, null, NOW)).isInstanceOf(InvalidCarDataException.class);
    }

    @Test
    @DisplayName("Acepta hasta el año siguiente al actual y rechaza años fuera de rango")
    void validatesYearRange() {
        assertThat(Car.create(withYear(2027), 1L, NOW).getYear()).isEqualTo(2027);
        assertThat(Car.create(withYear(Car.MIN_YEAR), 1L, NOW).getYear()).isEqualTo(Car.MIN_YEAR);

        assertThatThrownBy(() -> Car.create(withYear(2028), 1L, NOW)).isInstanceOf(InvalidCarDataException.class);
        assertThatThrownBy(() -> Car.create(withYear(1885), 1L, NOW)).isInstanceOf(InvalidCarDataException.class);
    }

    @Test
    @DisplayName("updateWith devuelve una nueva instancia y conserva id, dueño y fecha de creación")
    void updateWithKeepsIdentity() {
        Car original = Car.create(details, 10L, NOW).toBuilder().id(1L).build();
        LocalDateTime later = NOW.plusDays(1);

        Car updated = original.updateWith(new CarDetails("Mazda", "3", 2024, "XYZ-999", "Azul", "/img.jpg"), later);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getOwnerId()).isEqualTo(10L);
        assertThat(updated.getCreatedAt()).isEqualTo(NOW);
        assertThat(updated.getUpdatedAt()).isEqualTo(later);
        assertThat(updated.getBrand()).isEqualTo("Mazda");
        assertThat(updated.getPhotoUrl()).isEqualTo("/img.jpg");
        assertThat(original.getBrand()).isEqualTo("Toyota");
    }

    private CarDetails withYear(int year) {
        return new CarDetails("Toyota", "Corolla", year, "ABC-123", "Rojo", null);
    }
}
