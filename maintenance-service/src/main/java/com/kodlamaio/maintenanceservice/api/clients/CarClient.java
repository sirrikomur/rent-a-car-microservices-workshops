package com.kodlamaio.maintenanceservice.api.clients;

import com.kodlamaio.commonpackage.utils.enums.State;
import com.kodlamaio.maintenanceservice.business.dtos.responses.get.clients.GetCarClientResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "inventory-service", fallback = CarClientFallback.class)
public interface CarClient
{
    @GetMapping("/api/cars/{carId}")
    GetCarClientResponse getById(@PathVariable UUID carId);

    @PutMapping("/api/cars/{id}/change-state")
    void changeState(@PathVariable UUID id, @RequestBody State state);
}
