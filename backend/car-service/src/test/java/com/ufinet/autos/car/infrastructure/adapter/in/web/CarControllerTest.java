package com.ufinet.autos.car.infrastructure.adapter.in.web;

import com.ufinet.autos.car.application.port.in.CreateCarUseCase;
import com.ufinet.autos.car.application.port.in.DeleteCarUseCase;
import com.ufinet.autos.car.application.port.in.GetCarUseCase;
import com.ufinet.autos.car.application.port.in.SearchCarsUseCase;
import com.ufinet.autos.car.application.port.in.UpdateCarUseCase;
import com.ufinet.autos.car.domain.exception.CarNotFoundException;
import com.ufinet.autos.car.domain.exception.InvalidCarDataException;
import com.ufinet.autos.car.domain.exception.PlateAlreadyRegisteredException;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarDetails;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import com.ufinet.autos.car.infrastructure.adapter.in.web.mapper.CarWebMapper;
import com.ufinet.autos.car.infrastructure.config.ClockConfig;
import com.ufinet.autos.car.infrastructure.security.JwtTokenValidator;
import com.ufinet.autos.car.infrastructure.security.SecurityConfig;
import com.ufinet.autos.car.support.TestTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del adaptador REST con la seguridad JWT real y los casos de uso simulados.
 */
@WebMvcTest(CarController.class)
@Import({SecurityConfig.class, JwtTokenValidator.class, ClockConfig.class, CarWebMapper.class})
class CarControllerTest {

    private static final long USER_ID = 5L;
    private static final String VALID_BODY = """
            {"brand":"Toyota","model":"Corolla","year":2022,"plateNumber":"abc-123","color":"Rojo"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCarUseCase createCarUseCase;
    @MockitoBean
    private UpdateCarUseCase updateCarUseCase;
    @MockitoBean
    private DeleteCarUseCase deleteCarUseCase;
    @MockitoBean
    private GetCarUseCase getCarUseCase;
    @MockitoBean
    private SearchCarsUseCase searchCarsUseCase;

    private final Car car = Car.builder()
            .id(1L).brand("Toyota").model("Corolla").year(2022).plateNumber("ABC-123").color("Rojo")
            .ownerId(USER_ID)
            .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
            .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
            .build();

    private static String bearer() {
        return "Bearer " + TestTokens.validToken(USER_ID);
    }

    @Test
    @DisplayName("Sin token -> 401 y no se ejecuta ningún caso de uso")
    void requiresToken() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(searchCarsUseCase);
    }

    @Test
    @DisplayName("Token expirado -> 401")
    void rejectsExpiredToken() throws Exception {
        mockMvc.perform(get("/api/cars").header(HttpHeaders.AUTHORIZATION, "Bearer " + TestTokens.expiredToken(USER_ID)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/cars usa el id del token y los filtros de la query")
    void listWithFilters() throws Exception {
        when(searchCarsUseCase.search(new CarSearchCriteria(USER_ID, "cor", "Toyota", 2022))).thenReturn(List.of(car));

        mockMvc.perform(get("/api/cars")
                        .param("search", "cor").param("brand", "Toyota").param("year", "2022")
                        .header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].plateNumber").value("ABC-123"));
    }

    @Test
    @DisplayName("GET /api/cars?year=abc -> 400")
    void invalidYearParam() throws Exception {
        mockMvc.perform(get("/api/cars").param("year", "abc").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cars/brands -> marcas del usuario")
    void brands() throws Exception {
        when(searchCarsUseCase.findBrands(USER_ID)).thenReturn(List.of("Mazda", "Toyota"));

        mockMvc.perform(get("/api/cars/brands").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("Toyota"));
    }

    @Test
    @DisplayName("GET /api/cars/{id} de otro usuario -> 404")
    void getNotFound() throws Exception {
        when(getCarUseCase.getById(USER_ID, 99L)).thenThrow(new CarNotFoundException(99L));

        mockMvc.perform(get("/api/cars/99").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
    }

    @Test
    @DisplayName("POST /api/cars -> 201 con Location")
    void create() throws Exception {
        when(createCarUseCase.create(eq(USER_ID), any(CarDetails.class))).thenReturn(car);

        mockMvc.perform(post("/api/cars")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/cars/1")))
                .andExpect(jsonPath("$.brand").value("Toyota"));
    }

    @Test
    @DisplayName("POST /api/cars con datos inválidos -> 400 con errores por campo")
    void createInvalid() throws Exception {
        mockMvc.perform(post("/api/cars")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"brand":"","model":"Corolla","plateNumber":"*","color":"Rojo"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.brand").exists())
                .andExpect(jsonPath("$.errors.year").exists())
                .andExpect(jsonPath("$.errors.plateNumber").exists());
        verifyNoInteractions(createCarUseCase);
    }

    @Test
    @DisplayName("POST /api/cars con regla de dominio violada -> 400")
    void createDomainValidation() throws Exception {
        when(createCarUseCase.create(eq(USER_ID), any(CarDetails.class)))
                .thenThrow(new InvalidCarDataException("El año debe estar entre 1886 y 2027"));

        mockMvc.perform(post("/api/cars")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El año debe estar entre 1886 y 2027"));
    }

    @Test
    @DisplayName("POST /api/cars con placa repetida -> 409")
    void createConflict() throws Exception {
        when(createCarUseCase.create(eq(USER_ID), any(CarDetails.class)))
                .thenThrow(new PlateAlreadyRegisteredException("ABC-123"));

        mockMvc.perform(post("/api/cars")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/cars/{id} -> 200")
    void update() throws Exception {
        when(updateCarUseCase.update(eq(USER_ID), eq(1L), any(CarDetails.class))).thenReturn(car);

        mockMvc.perform(put("/api/cars/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/cars/{id} -> 204")
    void deleteCar() throws Exception {
        mockMvc.perform(delete("/api/cars/1").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNoContent());
        verify(deleteCarUseCase).delete(USER_ID, 1L);
    }

    @Test
    @DisplayName("DELETE /api/cars/{id} inexistente -> 404")
    void deleteNotFound() throws Exception {
        doThrow(new CarNotFoundException(9L)).when(deleteCarUseCase).delete(USER_ID, 9L);

        mockMvc.perform(delete("/api/cars/9").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound());
    }
}
