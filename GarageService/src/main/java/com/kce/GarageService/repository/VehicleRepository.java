package com.kce.GarageService.repository;

import com.kce.GarageService.entity.VehicleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleRepository extends MongoRepository<VehicleEntity, String> {
    List<VehicleEntity> findByCustomerEmail(String customerEmail);
    boolean existsByLicensePlateAndCustomerEmail(String licensePlate, String customerEmail);
}