package com.shineride.operationservice.repository;

import com.shineride.operationservice.entity.WashBayEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WashBayRepository extends MongoRepository<WashBayEntity, String> {

    // ✅ only active washbays can be used by system
    List<WashBayEntity> findByActiveTrue();
}