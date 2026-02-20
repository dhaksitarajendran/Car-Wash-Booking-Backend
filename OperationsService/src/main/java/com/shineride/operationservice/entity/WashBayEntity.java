package com.shineride.operationservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wash_bays")
public class WashBayEntity {

    @Id
    private String id;

    private String washbayName;

    // ✅ how many cars can be handled at the same time
    private int capacity;

    // ✅ optional: can disable a bay without deleting
    private Boolean active = true;

    public WashBayEntity() {}

    public WashBayEntity(String id, String washbayName, int capacity, Boolean active) {
        this.id = id;
        this.washbayName = washbayName;
        this.capacity = capacity;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWashbayName() { return washbayName; }
    public void setWashbayName(String washbayName) { this.washbayName = washbayName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}