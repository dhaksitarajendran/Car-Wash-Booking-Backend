package com.kce.couponservice.dto;

import java.util.List;

public class AssignCouponDTO {

    private String couponCode;          // which coupon
    private List<String> userEmails;    // assign to multiple users

    public AssignCouponDTO() {}

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public List<String> getUserEmails() { return userEmails; }
    public void setUserEmails(List<String> userEmails) { this.userEmails = userEmails; }
}