package com.kce.couponservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "coupon_assignments")
@CompoundIndex(name = "uniq_coupon_user", def = "{'couponId': 1, 'userEmail': 1}", unique = true)
public class CouponAssignmentEntity {

    @Id
    private String id;

    private String couponId;        // references CouponEntity.id
    private String couponCode;      // denormalize for quick reads

    private String userEmail;

    private Boolean used = false;
    private LocalDateTime usedAt;

    private String bookingId;       // when used
    private String paymentId;       // when used

    private LocalDateTime assignedAt = LocalDateTime.now();

    public CouponAssignmentEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCouponId() { return couponId; }
    public void setCouponId(String couponId) { this.couponId = couponId; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}