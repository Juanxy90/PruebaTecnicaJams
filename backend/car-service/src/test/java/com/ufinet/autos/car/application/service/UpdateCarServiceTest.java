package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.exception.PlateAlreadyRegisteredException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCarServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-02T08:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 6, 1, 12, 0);

    @Mock
    private CarRepositoryPort carRepository;

    private UpdateCarService service;

    private final Car existing = Car.create(
            new CarDetails("Toyota", "Corolla", 2022, "ABC-123", "Rojo", null), 5L, CREATED)
            .toBuilder().id(1L).build();
    private final CarDetails newDetails = new CarDetails("Toyota", "Corolla Cross", 2023, "ABC-123", "Negro", null);

    @BeforeEach
    void setUp() {
        service = new UpdateCarService(carRepository, CLOCK);
    }

    @Test
    @DisplayName("Actualiza los datos del auto del usuario")
    void updatesCar() {
        when(carRepository.findByIdAndOwnerId(1L, 5L)).thenReturn(Optional.of(existing));
        when(carRepository.existsByPlateNumberAndIdNot("ABC-123", 1L)).thenReturn(false);
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        Car updated = service.update(5L, 1L, newDetails);

        assertThat(updated.getModel()).isEqualTo("Corolla Cross");
        assertThat(updated.getColor()).isEqualTo("Negro");
        assertThat(updated.getCreatedAt()).isEqualTo(CREATED);
        assertThat(updated.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 2, 8, 0));
    }

    @Test
    @DisplayName("No permite editar autos de otro usuario (404)")
    void cannotUpdateOthersCar() {
        when(carRepository.findByIdAndOwnerId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, 1L, newDetails)).isInstanceOf(CarNotFoundException.class);
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza cambiar a una placa que ya usa otro auto")
    void rejectsPlateOfAnotherCar() {
        when(carRepository.findByIdAndOwnerId(1L, 5L)).thenReturn(Optional.of(existing));
        when(carRepository.existsByPlateNumberAndIdNot("ABC-123", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(5L, 1L, newDetails))
                .isInstanceOf(PlateAlreadyRegisteredException.class);
        verify(carRepository, never()).save(any());
    }
}
