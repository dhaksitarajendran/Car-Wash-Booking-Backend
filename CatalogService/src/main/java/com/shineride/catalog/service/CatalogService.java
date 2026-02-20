package com.shineride.catalog.service;

import com.shineride.catalog.entity.AddonEntity;
import com.shineride.catalog.entity.ServiceEntity;
import com.shineride.catalog.entity.ServiceType;

import java.util.List;

public interface CatalogService {

    // ✅ PUBLIC
    List<ServiceEntity> getAllServices();

    // ✅ PUBLIC
    List<AddonEntity> getAddonsByServiceType(ServiceType type);

    // ✅ ADMIN
    AddonEntity createAddon(ServiceType type, AddonEntity addon);

    // ✅ ADMIN
    AddonEntity updateAddon(String addonId, AddonEntity updated);

    // ✅ ADMIN (recommended instead of delete)
    AddonEntity disableAddon(String addonId);

    // ✅ ADMIN
    ServiceEntity updateService(ServiceType type, ServiceEntity updated);
    List<AddonEntity> getAllAddonsAdmin();
    // ❌ must NOT allow deleting BASIC/MODERATE/PREMIUM
    void deleteService(ServiceType type);
}