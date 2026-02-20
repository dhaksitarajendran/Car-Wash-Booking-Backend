package com.kce.GarageService.service.impl;

import com.kce.GarageService.dto.AdminVehicleCreateDTO;
import com.kce.GarageService.dto.VehicleRequestDTO;
import com.kce.GarageService.entity.VehicleEntity;
import com.kce.GarageService.repository.VehicleRepository;
import com.kce.GarageService.service.GarageService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GarageServiceImpl implements GarageService {

    private final VehicleRepository vehicleRepository;

    public GarageServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehicleEntity addVehicle(VehicleRequestDTO dto, String loggedInEmail) {
        if (dto == null || dto.getLicensePlate() == null || dto.getLicensePlate().trim().isEmpty()) {
            throw new RuntimeException("licensePlate is required");
        }

        String plate = normalizePlate(dto.getLicensePlate());
        String owner = normalizeEmail(loggedInEmail);

        if (vehicleRepository.existsById(plate)) {
            throw new RuntimeException("Vehicle already exists with licensePlate = " + plate);
        }

        VehicleEntity v = new VehicleEntity();
        v.setLicensePlate(plate);
        v.setCustomerEmail(owner);
        v.setDescription(dto.getDescription());
        v.setBrand(dto.getBrand());
        v.setModel(dto.getModel());
        v.setColor(dto.getColor());
        v.setCreatedAt(LocalDateTime.now());
        v.setUpdatedAt(LocalDateTime.now());

        return vehicleRepository.save(v);
    }
    @Override
    public VehicleEntity adminAddVehicle(AdminVehicleCreateDTO dto) {
        if (dto == null || dto.getCustomerEmail() == null || dto.getCustomerEmail().trim().isEmpty()) {
            throw new RuntimeException("customerEmail is required");
        }
        if (dto.getLicensePlate() == null || dto.getLicensePlate().trim().isEmpty()) {
            throw new RuntimeException("licensePlate is required");
        }

        String owner = normalizeEmail(dto.getCustomerEmail());
        String plate = normalizePlate(dto.getLicensePlate());

        if (vehicleRepository.existsById(plate)) {
            throw new RuntimeException("Vehicle already exists with licensePlate = " + plate);
        }

        VehicleEntity v = new VehicleEntity();
        v.setLicensePlate(plate);
        v.setCustomerEmail(owner);
        v.setDescription(dto.getDescription());
        v.setBrand(dto.getBrand());
        v.setModel(dto.getModel());
        v.setColor(dto.getColor());
        v.setCreatedAt(java.time.LocalDateTime.now());
        v.setUpdatedAt(java.time.LocalDateTime.now());

        return vehicleRepository.save(v);
    }
    @Override
    public List<VehicleEntity> getMyVehicles(String loggedInEmail) {
        return vehicleRepository.findByCustomerEmail(normalizeEmail(loggedInEmail));
    }

    @Override
    public VehicleEntity updateMyVehicle(String licensePlate, VehicleRequestDTO dto, String loggedInEmail) {
        String plate = normalizePlate(licensePlate);
        String owner = normalizeEmail(loggedInEmail);

        VehicleEntity existing = vehicleRepository.findById(plate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + plate));

        if (existing.getCustomerEmail() == null || !existing.getCustomerEmail().equalsIgnoreCase(owner)) {
            throw new AccessDeniedException("You are not allowed to update this vehicle");
        }

        if (dto != null) {
            // _id (licensePlate) should never be changed
            existing.setDescription(dto.getDescription());
            existing.setBrand(dto.getBrand());
            existing.setModel(dto.getModel());
            existing.setColor(dto.getColor());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return vehicleRepository.save(existing);
    }

    @Override
    public void deleteMyVehicle(String licensePlate, String loggedInEmail) {
        String plate = normalizePlate(licensePlate);
        String owner = normalizeEmail(loggedInEmail);

        VehicleEntity existing = vehicleRepository.findById(plate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + plate));

        if (existing.getCustomerEmail() == null || !existing.getCustomerEmail().equalsIgnoreCase(owner)) {
            throw new AccessDeniedException("You are not allowed to delete this vehicle");
        }

        vehicleRepository.deleteById(plate);
    }

    @Override
    public List<VehicleEntity> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<VehicleEntity> getVehiclesByCustomerEmail(String email) {
        return vehicleRepository.findByCustomerEmail(normalizeEmail(email));
    }

    @Override
    public void assertVehicleOwner(String licensePlate, String loggedInEmail) {
        String plate = normalizePlate(licensePlate);
        String owner = normalizeEmail(loggedInEmail);

        boolean ok = vehicleRepository.existsByLicensePlateAndCustomerEmail(plate, owner);
        if (!ok) throw new AccessDeniedException("Vehicle does not belong to this customer");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }

    private String normalizePlate(String plate) {
        return plate == null ? "" : plate.toUpperCase().replaceAll("\\s+", "").trim();
    }
}