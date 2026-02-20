package com.shineride.operationservice.dto;

public class WashBayAssignmentRequestDTO {

    private String bookingId;
    private String employeeId;

    public WashBayAssignmentRequestDTO() {}

    public WashBayAssignmentRequestDTO(String bookingId, String employeeId) {
        this.bookingId = bookingId;
        this.employeeId = employeeId;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}