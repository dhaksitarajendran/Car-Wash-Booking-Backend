package com.shineride.bookingservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "service_progress")
public class ServiceProgress {

    @Id
    private String id;

    private String bookingId;

    private ServiceStage currentStage;  // enum instead of String

    private String updatedBy;
    private LocalDateTime updatedAt;

    public ServiceProgress() {}

    public ServiceProgress(String id, String bookingId,
                           ServiceStage currentStage, String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.currentStage = currentStage;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public ServiceStage getCurrentStage() { return currentStage; }
    public void setCurrentStage(ServiceStage currentStage) { this.currentStage = currentStage; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}