package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.model.Car;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCarServiceTest {

    @Mock
    private CarRepositoryPort carRepository;

    @InjectMocks
    private DeleteCarService service;

    @Test
    void deletesOwnCar() {
        when(carRepository.findByIdAndOwnerId(1L, 5L))
                .thenReturn(Optional.of(Car.builder().id(1L).ownerId(5L).build()));

        service.delete(5L, 1L);

        verify(carRepository).deleteById(1L);
    }

    @Test
    void cannotDeleteOthersCar() {
        when(carRepository.findByIdAndOwnerId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, 1L)).isInstanceOf(CarNotFoundException.class);
        verify(carRepository, never()).deleteById(any());
    }
}
