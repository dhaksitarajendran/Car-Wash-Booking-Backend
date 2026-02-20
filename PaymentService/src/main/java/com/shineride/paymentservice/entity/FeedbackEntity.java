package com.shineride.paymentservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "feedbacks")
public class FeedbackEntity {

    @Id
    private String id;

    private String bookingId;

    // who gave feedback
    private String customerEmail;     // from JWT subject

    // service context (helpful for filtering)
    private String serviceId;         // or ServiceType if you use BASIC/MODERATE/PREMIUM
    private String serviceName;       // optional (for display)

    // feedback fields
    private Integer rating;           // 1..5
    private String comment;

    // only allow feedback after booking is COMPLETED (you enforce in service layer)
    private Boolean active = true;    // soft delete recommended

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FeedbackEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}