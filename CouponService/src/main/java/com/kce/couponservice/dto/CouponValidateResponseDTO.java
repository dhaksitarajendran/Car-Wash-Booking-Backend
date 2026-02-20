package com.kce.couponservice.dto;

public class CouponValidateResponseDTO {

    private boolean eligible;

    private String couponCode;
    private String type;          // PERCENT / FLAT

    private Integer percent;
    private Double flatAmount;

    private Double minPurchase;

    private String reason;        // why not eligible (optional)

    public CouponValidateResponseDTO() {}

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPercent() { return percent; }
    public void setPercent(Integer percent) { this.percent = percent; }

    public Double getFlatAmount() { return flatAmount; }
    public void setFlatAmount(Double flatAmount) { this.flatAmount = flatAmount; }

    public Double getMinPurchase() { return minPurchase; }
    public void setMinPurchase(Double minPurchase) { this.minPurchase = minPurchase; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}