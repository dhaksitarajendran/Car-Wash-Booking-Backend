package com.shineride.catalog.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "services")
public class ServiceEntity {

    @Id
    private String id; // ✅ "BASIC" / "MODERATE" / "PREMIUM"

    private ServiceType serviceType;   // same as id, but easier for code

    private String serviceName;        // display name
    private String serviceDescription; // ✅ required by you
    private double basePrice;

    private boolean active = true;     // optional: allow “disable” but not delete

    public ServiceEntity() {}

    public ServiceEntity(String id, ServiceType serviceType, String serviceName,
                         String serviceDescription, double basePrice, boolean active) {
        this.id = id;
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.basePrice = basePrice;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        // keep id in sync if you want
        if (serviceType != null) this.id = serviceType.name();
    }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}