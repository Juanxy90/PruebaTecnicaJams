package com.ufinet.autos.car.application.service;

import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarQueryServiceTest {

    @Mock
    private CarRepositoryPort carRepository;

    @InjectMocks
    private CarQueryService service;

    @Test
    void getByIdReturnsOwnCar() {
        Car car = Car.builder().id(1L).ownerId(5L).build();
        when(carRepository.findByIdAndOwnerId(1L, 5L)).thenReturn(Optional.of(car));

        assertThat(service.getById(5L, 1L)).isEqualTo(car);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(carRepository.findByIdAndOwnerId(1L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(5L, 1L)).isInstanceOf(CarNotFoundException.class);
    }

    @Test
    void searchDelegatesToRepository() {
        CarSearchCriteria criteria = new CarSearchCriteria(5L, "cor", "Toyota", 2022);
        List<Car> cars = List.of(Car.builder().id(1L).build());
        when(carRepository.search(criteria)).thenReturn(cars);

        assertThat(service.search(criteria)).isEqualTo(cars);
    }

    @Test
    void findBrandsDelegatesToRepository() {
        when(carRepository.findDistinctBrandsByOwnerId(5L)).thenReturn(List.of("Mazda", "Toyota"));

        assertThat(service.findBrands(5L)).containsExactly("Mazda", "Toyota");
    }
}
