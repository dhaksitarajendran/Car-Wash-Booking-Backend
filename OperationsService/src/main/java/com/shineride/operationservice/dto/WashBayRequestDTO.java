package com.shineride.operationservice.dto;

public class WashBayRequestDTO {

    private String washbayName;
    private int capacity;

    public WashBayRequestDTO() {}

    public WashBayRequestDTO(String washbayName, int capacity) {
        this.washbayName = washbayName;
        this.capacity = capacity;
    }

    public String getWashbayName() { return washbayName; }
    public void setWashbayName(String washbayName) { this.washbayName = washbayName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}