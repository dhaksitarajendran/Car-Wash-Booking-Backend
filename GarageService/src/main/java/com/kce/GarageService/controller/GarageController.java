package com.kce.GarageService.controller;

import com.kce.GarageService.dto.AdminVehicleCreateDTO;
import com.kce.GarageService.dto.VehicleRequestDTO;
import com.kce.GarageService.entity.VehicleEntity;
import com.kce.GarageService.service.GarageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/garage")
public class GarageController {

    private final GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    // ✅ CUSTOMER ONLY (EMPLOYEE cannot add)
    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public VehicleEntity addVehicle(@RequestBody VehicleRequestDTO dto, Authentication auth) {
        return garageService.addVehicle(dto, auth.getName());
    }

    // ✅ CUSTOMER ONLY
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<VehicleEntity> myVehicles(Authentication auth) {
        return garageService.getMyVehicles(auth.getName());
    }

    // ✅ CUSTOMER ONLY
    @PutMapping("/{licensePlate}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public VehicleEntity updateMyVehicle(
            @PathVariable String licensePlate,
            @RequestBody VehicleRequestDTO dto,
            Authentication auth
    ) {
        return garageService.updateMyVehicle(licensePlate, dto, auth.getName());
    }

    // ✅ CUSTOMER ONLY
    @DeleteMapping("/{licensePlate}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteMyVehicle(@PathVariable String licensePlate, Authentication auth) {
        garageService.deleteMyVehicle(licensePlate, auth.getName());
        return "Deleted vehicle = " + licensePlate;
    }

    // ✅ ADMIN: create a vehicle for ANY customer
    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleEntity adminAddVehicle(@RequestBody AdminVehicleCreateDTO dto) {
        return garageService.adminAddVehicle(dto);
    }

    // ✅ ADMIN: list ALL vehicles of ALL users
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<VehicleEntity> allVehicles() {
        return garageService.getAllVehicles();
    }

    // ✅ ADMIN: find vehicles registered by a specific user
    @GetMapping("/by-customer")
    @PreAuthorize("hasRole('ADMIN')")
    public List<VehicleEntity> vehiclesByCustomer(@RequestParam String email) {
        return garageService.getVehiclesByCustomerEmail(email);
    }
}