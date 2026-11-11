package com.ufinet.autos.car.infrastructure.adapter.out.persistence;

import com.ufinet.autos.car.domain.exception.PlateAlreadyRegisteredException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import com.ufinet.autos.car.infrastructure.adapter.out.persistence.mapper.CarPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba del adaptador de persistencia y de las Specifications de búsqueda/filtrado
 * contra una base de datos en memoria (H2).
 */
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({CarPersistenceAdapter.class, CarPersistenceMapper.class})
class CarPersistenceAdapterTest {

    private static final long OWNER = 1L;
    private static final long OTHER_OWNER = 2L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Autowired
    private CarPersistenceAdapter adapter;

    private Car corolla;

    @BeforeEach
    void setUp() {
        corolla = adapter.save(car("Toyota", "Corolla", 2022, "ABC-123", OWNER, 0));
        adapter.save(car("Toyota", "Hilux", 2020, "HLX-200", OWNER, 1));
        adapter.save(car("Mazda", "CX-5", 2022, "MZD-555", OWNER, 2));
        adapter.save(car("Toyota", "Corolla", 2022, "OTR-999", OTHER_OWNER, 3));
    }

    private Car car(String brand, String model, int year, String plate, long owner, int minutesLater) {
        return Car.create(new CarDetails(brand, model, year, plate, "Blanco", null), owner, NOW.plusMinutes(minutesLater));
    }

    private List<String> plates(CarSearchCriteria criteria) {
        return adapter.search(criteria).stream().map(Car::getPlateNumber).toList();
    }

    @Test
    @DisplayName("Lista solo los autos del usuario, del más reciente al más antiguo")
    void listsOnlyOwnCars() {
        assertThat(plates(CarSearchCriteria.forOwner(OWNER))).containsExactly("MZD-555", "HLX-200", "ABC-123");
    }

    @Test
    @DisplayName("Busca por placa o modelo sin distinguir mayúsculas")
    void searchByPlateOrModel() {
        assertThat(plates(new CarSearchCriteria(OWNER, "coro", null, null))).containsExactly("ABC-123");
        assertThat(plates(new CarSearchCriteria(OWNER, "hlx", null, null))).containsExactly("HLX-200");
    }

    @Test
    @DisplayName("Filtra por marca y por año (combinables)")
    void filterByBrandAndYear() {
        assertThat(plates(new CarSearchCriteria(OWNER, null, "toyota", null))).containsExactlyInAnyOrder("ABC-123", "HLX-200");
        assertThat(plates(new CarSearchCriteria(OWNER, null, null, 2022))).containsExactlyInAnyOrder("ABC-123", "MZD-555");
        assertThat(plates(new CarSearchCriteria(OWNER, null, "Toyota", 2022))).containsExactly("ABC-123");
    }

    @Test
    @DisplayName("Los comodines de LIKE se tratan como texto literal")
    void escapesLikeWildcards() {
        assertThat(plates(new CarSearchCriteria(OWNER, "%", null, null))).isEmpty();
        assertThat(plates(new CarSearchCriteria(OWNER, "_", null, null))).isEmpty();
    }

    @Test
    @DisplayName("Marcas distintas del usuario, ordenadas")
    void distinctBrands() {
        assertThat(adapter.findDistinctBrandsByOwnerId(OWNER)).containsExactly("Mazda", "Toyota");
    }

    @Test
    @DisplayName("findByIdAndOwnerId no devuelve autos de otros usuarios")
    void findByIdAndOwner() {
        assertThat(adapter.findByIdAndOwnerId(corolla.getId(), OWNER)).isPresent();
        assertThat(adapter.findByIdAndOwnerId(corolla.getId(), OTHER_OWNER)).isEmpty();
    }

    @Test
    @DisplayName("Verifica la unicidad de la placa")
    void plateUniqueness() {
        assertThat(adapter.existsByPlateNumber("ABC-123")).isTrue();
        assertThat(adapter.existsByPlateNumberAndIdNot("ABC-123", corolla.getId())).isFalse();
        assertThatThrownBy(() -> adapter.save(car("Kia", "Rio", 2021, "ABC-123", OWNER, 5)))
                .isInstanceOf(PlateAlreadyRegisteredException.class);
    }

    @Test
    @DisplayName("Elimina un auto")
    void deleteCar() {
        adapter.deleteById(corolla.getId());
        assertThat(adapter.findByIdAndOwnerId(corolla.getId(), OWNER)).isEmpty();
    }
}
