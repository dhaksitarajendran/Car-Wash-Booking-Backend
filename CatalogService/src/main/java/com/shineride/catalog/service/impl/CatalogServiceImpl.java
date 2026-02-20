package com.shineride.catalog.service.impl;

import com.shineride.catalog.entity.AddonEntity;
import com.shineride.catalog.entity.ServiceEntity;
import com.shineride.catalog.entity.ServiceType;
import com.shineride.catalog.repository.AddonRepository;
import com.shineride.catalog.repository.ServiceRepository;
import com.shineride.catalog.service.CatalogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final ServiceRepository serviceRepository;
    private final AddonRepository addonRepository;

    public CatalogServiceImpl(ServiceRepository serviceRepository, AddonRepository addonRepository) {
        this.serviceRepository = serviceRepository;
        this.addonRepository = addonRepository;
    }

    // ✅ PUBLIC
    @Override
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    // ✅ PUBLIC
    @Override
    public List<AddonEntity> getAddonsByServiceType(ServiceType type) {
    	 return addonRepository.findByServiceType(type)
    	            .stream()
    	            .filter(a -> a.getActive() == null || a.getActive())
    	            .toList();
    }
    @Override
    public List<AddonEntity> getAllAddonsAdmin() {
        return addonRepository.findAll();
    }
    // ✅ ADMIN: create addon for a specific service type
    @Override
    public AddonEntity createAddon(ServiceType type, AddonEntity addon) {

        if (addon == null) throw new RuntimeException("Addon cannot be null");
        if (addon.getAddonName() == null || addon.getAddonName().trim().isEmpty())
            throw new RuntimeException("Addon name is required");

        // ✅ attach service
        addon.setServiceType(type);

        // ✅ normalize for uniqueness
        addon.setAddonNameKey(normalize(addon.getAddonName()));
        addon.setAddonDescKey(normalize(addon.getAddonDescription()));

        // ✅ defaults
        addon.setId(null);
        addon.setActive(addon.getActive() == null ? true : addon.getActive());

        return addonRepository.save(addon);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }
    // ✅ ADMIN: update addon
    @Override
    public AddonEntity updateAddon(String addonId, AddonEntity updated) {

        AddonEntity existing = addonRepository.findById(addonId)
                .orElseThrow(() -> new RuntimeException("Addon not found: " + addonId));

        existing.setAddonName(updated.getAddonName());
        existing.setAddonDescription(updated.getAddonDescription());
        existing.setAddonPrice(updated.getAddonPrice());

        // ✅ keep keys in sync
        existing.setAddonNameKey(normalize(existing.getAddonName()));
        existing.setAddonDescKey(normalize(existing.getAddonDescription()));

        return addonRepository.save(existing);
    }

    // ✅ ADMIN: disable addon (soft delete)
    @Override
    public AddonEntity disableAddon(String addonId) {

        AddonEntity existing = addonRepository.findById(addonId)
                .orElseThrow(() -> new RuntimeException("Addon not found: " + addonId));

        existing.setActive(false);
        return addonRepository.save(existing);
    }

    // ✅ ADMIN: update service details (name, desc, price)
    @Override
    public ServiceEntity updateService(ServiceType type, ServiceEntity updated) {

        String id = type.name(); // BASIC / MODERATE / PREMIUM

        ServiceEntity existing = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found: " + id));

        existing.setServiceName(updated.getServiceName());
        existing.setServiceDescription(updated.getServiceDescription());
        existing.setBasePrice(updated.getBasePrice());

        return serviceRepository.save(existing);
    }

    // ❌ ADMIN cannot delete BASIC/MODERATE/PREMIUM
    @Override
    public void deleteService(ServiceType type) {
        throw new RuntimeException("Core services BASIC/MODERATE/PREMIUM cannot be deleted");
    }
}