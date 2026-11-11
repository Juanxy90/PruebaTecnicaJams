package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCarServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CarRepositoryPort carRepository;

    private CreateCarService service;
    private final CarDetails details = new CarDetails("Toyota", "Corolla", 2022, "abc-123", "Rojo", null);

    @BeforeEach
    void setUp() {
        service = new CreateCarService(carRepository, CLOCK);
    }

    @Test
    @DisplayName("Crea el auto asociado al usuario autenticado")
    void createsCar() {
        when(carRepository.existsByPlateNumber("ABC-123")).thenReturn(false);
        when(carRepository.save(any(Car.class)))
                .thenAnswer(inv -> inv.<Car>getArgument(0).toBuilder().id(1L).build());

        Car created = service.create(5L, details);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getOwnerId()).isEqualTo(5L);
        assertThat(created.getPlateNumber()).isEqualTo("ABC-123");
        assertThat(created.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 12, 0));
    }

    @Test
    @DisplayName("Rechaza placas ya registradas")
    void rejectsDuplicatedPlate() {
        when(carRepository.existsByPlateNumber("ABC-123")).thenReturn(true);

        assertThatThrownBy(() -> service.create(5L, details)).isInstanceOf(PlateAlreadyRegisteredException.class);
        verify(carRepository, never()).save(any());
    }
}
