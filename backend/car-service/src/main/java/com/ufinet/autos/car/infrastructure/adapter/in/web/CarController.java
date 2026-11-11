package com.ufinet.autos.car.infrastructure.adapter.in.web;

import com.ufinet.autos.car.application.port.in.CreateCarUseCase;
import com.ufinet.autos.car.application.port.in.DeleteCarUseCase;
import com.ufinet.autos.car.application.port.in.GetCarUseCase;
import com.ufinet.autos.car.application.port.in.SearchCarsUseCase;
import com.ufinet.autos.car.application.port.in.UpdateCarUseCase;
import com.ufinet.autos.car.domain.model.Car;
import com.ufinet.autos.car.domain.model.CarSearchCriteria;
import com.ufinet.autos.car.infrastructure.adapter.in.web.dto.CarRequest;
import com.ufinet.autos.car.infrastructure.adapter.in.web.dto.CarResponse;
import com.ufinet.autos.car.infrastructure.adapter.in.web.mapper.CarWebMapper;
import com.ufinet.autos.car.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Adaptador de entrada REST para la gestión de autos del usuario autenticado.
 * El id del usuario SIEMPRE se toma del JWT, nunca del cuerpo de la petición.
 */
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Tag(name = "Autos", description = "CRUD, búsqueda y filtrado de autos del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class CarController {

    private final CreateCarUseCase createCarUseCase;
    private final UpdateCarUseCase updateCarUseCase;
    private final DeleteCarUseCase deleteCarUseCase;
    private final GetCarUseCase getCarUseCase;
    private final SearchCarsUseCase searchCarsUseCase;
    private final CarWebMapper mapper;

    @GetMapping
    @Operation(summary = "Listar autos", description = "Lista los autos del usuario; permite buscar por placa/modelo y filtrar por marca/año")
    public List<CarResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                  @Parameter(description = "Texto a buscar en placa o modelo")
                                  @RequestParam(required = false) String search,
                                  @Parameter(description = "Filtrar por marca")
                                  @RequestParam(required = false) String brand,
                                  @Parameter(description = "Filtrar por año")
                                  @RequestParam(required = false) Integer year) {
        CarSearchCriteria criteria = new CarSearchCriteria(user.id(), search, brand, year);
        return searchCarsUseCase.search(criteria).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/brands")
    @Operation(summary = "Marcas registradas", description = "Marcas distintas de los autos del usuario (para el filtro)")
    public List<String> brands(@AuthenticationPrincipal AuthenticatedUser user) {
        return searchCarsUseCase.findBrands(user.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener auto por id")
    public CarResponse getById(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return mapper.toResponse(getCarUseCase.getById(user.id(), id));
    }

    @PostMapping
    @Operation(summary = "Crear auto")
    public ResponseEntity<CarResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
                                              @Valid @RequestBody CarRequest request) {
        Car created = createCarUseCase.create(user.id(), mapper.toDetails(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar auto")
    public CarResponse update(@AuthenticationPrincipal AuthenticatedUser user,
                              @PathVariable Long id,
                              @Valid @RequestBody CarRequest request) {
        return mapper.toResponse(updateCarUseCase.update(user.id(), id, mapper.toDetails(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar auto")
    public void delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        deleteCarUseCase.delete(user.id(), id);
    }
}
