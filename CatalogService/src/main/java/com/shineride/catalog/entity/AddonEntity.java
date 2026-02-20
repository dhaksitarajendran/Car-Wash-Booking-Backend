package com.shineride.catalog.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "addons")
@CompoundIndex(
		  name = "uniq_service_addon_name",
		  def = "{'serviceType': 1, 'addonNameKey': 1}",
		  unique = true
		)
public class AddonEntity {

    @Id
    private String id;

    private ServiceType serviceType;

    private String addonName;
    private String addonDescription;
    private double addonPrice;

    // ✅ use Boolean to avoid null boolean issues
    private Boolean active = true;

    // ✅ normalized fields for uniqueness
    @Indexed
    private String addonNameKey;

    @Indexed
    private String addonDescKey;

    public AddonEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public String getAddonName() { return addonName; }
    public void setAddonName(String addonName) { this.addonName = addonName; }

    public String getAddonDescription() { return addonDescription; }
    public void setAddonDescription(String addonDescription) { this.addonDescription = addonDescription; }

    public double getAddonPrice() { return addonPrice; }
    public void setAddonPrice(double addonPrice) { this.addonPrice = addonPrice; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getAddonNameKey() { return addonNameKey; }
    public void setAddonNameKey(String addonNameKey) { this.addonNameKey = addonNameKey; }

    public String getAddonDescKey() { return addonDescKey; }
    public void setAddonDescKey(String addonDescKey) { this.addonDescKey = addonDescKey; }
}