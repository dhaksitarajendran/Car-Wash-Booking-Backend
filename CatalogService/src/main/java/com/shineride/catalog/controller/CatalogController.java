package com.shineride.catalog.controller;

import com.shineride.catalog.entity.AddonEntity;
import com.shineride.catalog.entity.ServiceEntity;
import com.shineride.catalog.entity.ServiceType;
import com.shineride.catalog.service.CatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // ✅ PUBLIC: list the 3 core services (BASIC/MODERATE/PREMIUM)
    @GetMapping("/services")
    public List<ServiceEntity> getServices() {
        return catalogService.getAllServices();
    }

    // ✅ PUBLIC: list addons for a specific service type
    @GetMapping("/services/{type}/addons")
    public List<AddonEntity> getAddonsByService(@PathVariable ServiceType type) {
        return catalogService.getAddonsByServiceType(type);
    }
    @GetMapping("/addons/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AddonEntity> allAddons() {
        return catalogService.getAllAddonsAdmin();
    }
    // ✅ ADMIN ONLY: create addon for a service (BASIC/MODERATE/PREMIUM)
    @PostMapping("/services/{type}/addons")
    @PreAuthorize("hasRole('ADMIN')")
    public AddonEntity createAddon(@PathVariable ServiceType type, @RequestBody AddonEntity addon) {
        return catalogService.createAddon(type, addon);
    }

    // ✅ ADMIN ONLY: update addon (price/desc/name) — cannot move addon to another service
    @PutMapping("/addons/{addonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public AddonEntity updateAddon(@PathVariable String addonId, @RequestBody AddonEntity updated) {
        return catalogService.updateAddon(addonId, updated);
    }

    // ✅ ADMIN ONLY: disable addon (recommended instead of delete)
    @PutMapping("/addons/{addonId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public AddonEntity disableAddon(@PathVariable String addonId) {
        return catalogService.disableAddon(addonId);
    }

    // ✅ ADMIN ONLY: update core service details (price/description/name)
    @PutMapping("/services/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceEntity updateService(@PathVariable ServiceType type, @RequestBody ServiceEntity updated) {
        return catalogService.updateService(type, updated);
    }

    // ❌ ADMIN cannot delete BASIC/MODERATE/PREMIUM (always forbidden by service logic)
    @DeleteMapping("/services/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteService(@PathVariable ServiceType type) {
        catalogService.deleteService(type);
        return "Deleted service = " + type;
    }
}