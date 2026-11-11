package com.ufinet.autos.car.infrastructure.adapter.out.persistence.repository;

import com.ufinet.autos.car.infrastructure.adapter.out.persistence.entity.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCarRepository extends JpaRepository<CarEntity, Long>, JpaSpecificationExecutor<CarEntity> {

    Optional<CarEntity> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndIdNot(String plateNumber, Long id);

    @Query("select distinct c.brand from CarEntity c where c.ownerId = :ownerId order by c.brand")
    List<String> findDistinctBrandsByOwnerId(@Param("ownerId") Long ownerId);
}
