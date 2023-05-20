package com.kodlamaio.maintenanceservice.business.concretes;


import com.kodlamaio.commonpackage.utils.enums.State;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.maintenanceservice.api.clients.CarClient;
import com.kodlamaio.maintenanceservice.business.abstracts.MaintenanceService;
import com.kodlamaio.maintenanceservice.business.dtos.requests.create.CreateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dtos.requests.update.UpdateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dtos.responses.create.CreateMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dtos.responses.get.GetAllMaintenancesResponse;
import com.kodlamaio.maintenanceservice.business.dtos.responses.get.GetMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dtos.responses.update.UpdateMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.rules.MaintenanceBusinessRules;
import com.kodlamaio.maintenanceservice.entities.Maintenance;
import com.kodlamaio.maintenanceservice.repository.MaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceManager implements MaintenanceService
{
    private final MaintenanceRepository repository;
    private final ModelMapperService mapper;
    private final KafkaProducer producer;
    private final MaintenanceBusinessRules rules;
    private final CarClient carClient;

    @Override
    public List<GetAllMaintenancesResponse> getAll() {
        List<Maintenance> maintenances = repository.findAll();
        List<GetAllMaintenancesResponse> response = maintenances
                .stream()
                .map(maintenance -> mapper.forResponse().map(maintenance, GetAllMaintenancesResponse.class))
                .toList();

        return response;
    }

    @Override
    public GetMaintenanceResponse getById(UUID id) {
        rules.checkIfMaintenanceExists(id);
        Maintenance maintenance = repository.findById(id).orElseThrow();
        GetMaintenanceResponse response = mapper.forResponse().map(maintenance, GetMaintenanceResponse.class);

        return response;
    }

    @Override
    public GetMaintenanceResponse returnCarFromMaintenance(UUID carId) {
        rules.checkIfCarIsNotUnderMaintenance(carId);
        Maintenance maintenance = repository.findByCarIdAndIsCompletedIsFalse(carId);
        maintenance.setCompleted(true);
        maintenance.setEndDate(LocalDateTime.now());
        repository.save(maintenance); // Update
        carClient.changeState(carId, State.Available);
        GetMaintenanceResponse response = mapper.forResponse().map(maintenance, GetMaintenanceResponse.class);

        return response;
    }

    @Override
    public CreateMaintenanceResponse add(CreateMaintenanceRequest request) {
        rules.checkIfCarUnderMaintenance(request.getCarId());
        rules.checkCarAvailabilityForMaintenance(carClient.getById(request.getCarId()).getState());
        Maintenance maintenance = mapper.forRequest().map(request, Maintenance.class);
        //maintenance.setId(0);
        maintenance.setCompleted(false);
        maintenance.setStartDate(LocalDateTime.now());
        maintenance.setEndDate(null);
        repository.save(maintenance);
        carClient.changeState(request.getCarId(), State.Maintenance);
        CreateMaintenanceResponse response = mapper.forResponse().map(maintenance, CreateMaintenanceResponse.class);

        return response;
    }

    @Override
    public UpdateMaintenanceResponse update(UUID id, UpdateMaintenanceRequest request) {
        rules.checkIfMaintenanceExists(id);
        Maintenance maintenance = mapper.forRequest().map(request, Maintenance.class);
        maintenance.setId(id);
        repository.save(maintenance);
        UpdateMaintenanceResponse response = mapper.forResponse().map(maintenance, UpdateMaintenanceResponse.class);

        return response;
    }

    @Override
    public void delete(UUID id) {
        rules.checkIfMaintenanceExists(id);
        makeCarAvailableIfIsCompletedFalse(id);
        repository.deleteById(id);
        carClient.changeState(repository.findById(id).orElseThrow().getCarId(), State.Available);
    }

    private void makeCarAvailableIfIsCompletedFalse(UUID id) {
        UUID carId = repository.findById(id).get().getCarId();
        if (repository.existsByCarIdAndIsCompletedIsFalse(carId)) {
            carClient.changeState(carId, State.Available);
        }
    }
}
