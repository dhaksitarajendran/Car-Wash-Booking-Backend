package com.shineride.bookingservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

@Document(collection = "bookings")
public class BookingEntity {

    @Id
    private String id;

    // ✅ Owner of booking (set automatically from JWT)
    private String customerEmail;

    private String userId;
    private String serviceId;
    private List<String> addOnIds;
    private String employeeId;
    @JsonAlias("totalAmount")
    private Double totalCost;
    private String pickupDate;
    private String dropDate;

    // ✅ status for delete/cancel logic
    private String status; // ACTIVE / CANCELLED

    // ✅ Vehicle being washed (licensePlate from GarageService)
    @JsonAlias("licensePlate")
    private String vehicleId;

    private String address;

    public BookingEntity() {
    }

    public BookingEntity(String id, String customerEmail, String userId, String serviceId,
            List<String> addOnIds, String employeeId, Double totalCost, String status,
            String pickupDate, String dropDate, String address) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.userId = userId;
        this.serviceId = serviceId;
        this.addOnIds = addOnIds;
        this.employeeId = employeeId;
        this.totalCost = totalCost;
        this.status = status;
        this.pickupDate = pickupDate;
        this.dropDate = dropDate;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getAddOnIds() {
        return addOnIds;
    }

    public void setAddOnIds(List<String> addOnIds) {
        this.addOnIds = addOnIds;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getDropDate() {
        return dropDate;
    }

    public void setDropDate(String dropDate) {
        this.dropDate = dropDate;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}