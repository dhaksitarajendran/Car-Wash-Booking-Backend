package com.shineride.paymentservice.service;

import com.shineride.paymentservice.entity.PaymentEntity;

import java.util.List;

public interface PaymentService {

    // ✅ CUSTOMER: creates razorpay order + payment record (coupon optional)
    PaymentEntity createRazorpayOrder(String bookingId, String couponCode, String customerEmail);

    // ✅ CUSTOMER: verify razorpay signature + mark paid
    PaymentEntity verifyRazorpayPayment(
            String paymentId,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature,
            String customerEmail
    );

    // ✅ CUSTOMER
    List<PaymentEntity> getMyPayments(String customerEmail);

    // ✅ ADMIN
    List<PaymentEntity> getAllPayments();
}