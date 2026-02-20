package com.kce.couponservice.service;

import com.kce.couponservice.dto.AssignCouponDTO;
import com.kce.couponservice.dto.CouponRequestDTO;
import com.kce.couponservice.dto.CouponValidateResponseDTO;
import com.kce.couponservice.entity.CouponAssignmentEntity;
import com.kce.couponservice.entity.CouponEntity;

import java.util.List;
import java.util.Map;

public interface CouponService {

    // ADMIN
    CouponEntity createCoupon(CouponRequestDTO req);

    CouponEntity updateCoupon(String couponCode, CouponRequestDTO req);

    List<CouponAssignmentEntity> assignCouponToUsers(AssignCouponDTO dto);

    List<CouponEntity> getAllCoupons();

    List<CouponAssignmentEntity> getAssignmentsForCoupon(String couponCode);

    Map<String, Object> getCouponStats(String couponCode);

    // CUSTOMER
    List<CouponAssignmentEntity> getMyCoupons(String userEmail);

    // INTERNAL (PaymentService)
    CouponValidateResponseDTO validateCoupon(String userEmail, String couponCode, Double amount);

    void markCouponUsed(String userEmail, String couponCode, String bookingId, String paymentId);
}