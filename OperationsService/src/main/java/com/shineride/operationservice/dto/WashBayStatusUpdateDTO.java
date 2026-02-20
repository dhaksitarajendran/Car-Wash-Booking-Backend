package com.shineride.operationservice.dto;

public class WashBayStatusUpdateDTO {

    private String status;   // ASSIGNED / IN_PROGRESS / COMPLETED / CANCELLED

    public WashBayStatusUpdateDTO() {}

    public WashBayStatusUpdateDTO(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}