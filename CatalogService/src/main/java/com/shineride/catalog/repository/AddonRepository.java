package com.shineride.catalog.repository;

import com.shineride.catalog.entity.AddonEntity;
import com.shineride.catalog.entity.ServiceType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AddonRepository extends MongoRepository<AddonEntity, String> {

    List<AddonEntity> findByServiceType(ServiceType serviceType);

    List<AddonEntity> findByServiceTypeAndActiveTrue(ServiceType serviceType);
}