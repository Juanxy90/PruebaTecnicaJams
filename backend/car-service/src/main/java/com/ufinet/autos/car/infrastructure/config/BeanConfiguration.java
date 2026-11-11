package com.ufinet.autos.car.infrastructure.config;

import com.ufinet.autos.car.application.port.in.CreateCarUseCase;
import com.ufinet.autos.car.application.port.in.DeleteCarUseCase;
import com.ufinet.autos.car.application.port.in.UpdateCarUseCase;
import com.ufinet.autos.car.application.port.out.CarRepositoryPort;
import com.ufinet.autos.car.application.service.CarQueryService;
import com.ufinet.autos.car.application.service.CreateCarService;
import com.ufinet.autos.car.application.service.DeleteCarService;
import com.ufinet.autos.car.application.service.UpdateCarService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition Root: conecta los casos de uso (POJOs) con los adaptadores.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public CreateCarUseCase createCarUseCase(CarRepositoryPort carRepository, Clock clock) {
        return new CreateCarService(carRepository, clock);
    }

    @Bean
    public UpdateCarUseCase updateCarUseCase(CarRepositoryPort carRepository, Clock clock) {
        return new UpdateCarService(carRepository, clock);
    }

    @Bean
    public DeleteCarUseCase deleteCarUseCase(CarRepositoryPort carRepository) {
        return new DeleteCarService(carRepository);
    }

    /** Implementa GetCarUseCase y SearchCarsUseCase (lecturas). */
    @Bean
    public CarQueryService carQueryService(CarRepositoryPort carRepository) {
        return new CarQueryService(carRepository);
    }
}
