package com.kce.GarageService.service;

import com.kce.GarageService.dto.AdminVehicleCreateDTO;
import com.kce.GarageService.dto.VehicleRequestDTO;
import com.kce.GarageService.entity.VehicleEntity;

import java.util.List;

public interface GarageService {

    VehicleEntity addVehicle(VehicleRequestDTO dto, String loggedInEmail);

    List<VehicleEntity> getMyVehicles(String loggedInEmail);

    VehicleEntity updateMyVehicle(String licensePlate, VehicleRequestDTO dto, String loggedInEmail);

    void deleteMyVehicle(String licensePlate, String loggedInEmail);

    // ✅ admin features
    VehicleEntity adminAddVehicle(AdminVehicleCreateDTO dto);

    List<VehicleEntity> getAllVehicles();

    List<VehicleEntity> getVehiclesByCustomerEmail(String email);

    void assertVehicleOwner(String licensePlate, String loggedInEmail);
}