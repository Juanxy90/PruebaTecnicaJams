package com.ufinet.autos.car.domain.model;

import com.ufinet.autos.car.domain.exception.InvalidCarDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarDetailsTest {

    @Test
    @DisplayName("Normaliza textos y placa (mayúsculas, sin espacios)")
    void normalizesData() {
        CarDetails details = new CarDetails(" Toyota ", " Corolla ", 2022, " abc 123 ", " Rojo ", "  ");

        assertThat(details.brand()).isEqualTo("Toyota");
        assertThat(details.model()).isEqualTo("Corolla");
        assertThat(details.plateNumber()).isEqualTo("ABC123");
        assertThat(details.color()).isEqualTo("Rojo");
        assertThat(details.photoUrl()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("La marca es obligatoria")
    void brandIsRequired(String brand) {
        assertThatThrownBy(() -> new CarDetails(brand, "Corolla", 2022, "ABC123", "Rojo", null))
                .isInstanceOf(InvalidCarDataException.class)
                .hasMessageContaining("marca");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB1", "ABC_123", "ABCDEFGHIJK", "ÑAB123"})
    @DisplayName("Rechaza placas con formato inválido")
    void rejectsInvalidPlate(String plate) {
        assertThatThrownBy(() -> new CarDetails("Toyota", "Corolla", 2022, plate, "Rojo", null))
                .isInstanceOf(InvalidCarDataException.class)
                .hasMessageContaining("placa");
    }

    @Test
    @DisplayName("El año es obligatorio")
    void yearIsRequired() {
        assertThatThrownBy(() -> new CarDetails("Toyota", "Corolla", null, "ABC123", "Rojo", null))
                .isInstanceOf(InvalidCarDataException.class);
    }

    @Test
    @DisplayName("Rechaza textos más largos de lo permitido")
    void rejectsTooLongText() {
        String longModel = "x".repeat(CarDetails.MAX_MODEL_LENGTH + 1);
        assertThatThrownBy(() -> new CarDetails("Toyota", longModel, 2022, "ABC123", "Rojo", null))
                .isInstanceOf(InvalidCarDataException.class);
    }
}
