package com.kce.couponservice.dto;

import com.kce.couponservice.entity.DiscountType;
import java.time.LocalDateTime;

public class CouponRequestDTO {

    private String couponCode;
    private String description;

    private DiscountType discountType;  // PERCENT / FLAT
    private Integer discountPercent;    // if PERCENT
    private Double flatAmount;          // if FLAT

    private Double minOrderAmount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    private Boolean active = true;

    public CouponRequestDTO() {}

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public Double getFlatAmount() { return flatAmount; }
    public void setFlatAmount(Double flatAmount) { this.flatAmount = flatAmount; }

    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}