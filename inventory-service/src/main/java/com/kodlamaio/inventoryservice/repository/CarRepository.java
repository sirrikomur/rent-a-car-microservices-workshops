package com.kodlamaio.inventoryservice.repository;

import com.kodlamaio.commonpackage.utils.enums.State;
import com.kodlamaio.inventoryservice.entities.Car;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    @Modifying
    @Transactional
    @Query(value = "update Car c set c.state = :state where c.id = :id")
    void changeStateByCarId(State state, @Param("id") UUID id);
}
