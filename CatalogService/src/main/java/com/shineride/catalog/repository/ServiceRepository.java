package com.shineride.catalog.repository;

import com.shineride.catalog.entity.ServiceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServiceRepository extends MongoRepository<ServiceEntity, String> {
}