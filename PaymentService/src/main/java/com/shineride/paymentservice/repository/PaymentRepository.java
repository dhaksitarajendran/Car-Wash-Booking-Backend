package com.shineride.paymentservice.repository;

import com.shineride.paymentservice.entity.PaymentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<PaymentEntity, String> {

    // ✅ customer views only their payments
    List<PaymentEntity> findByCustomerEmail(String customerEmail);

    // ✅ get payment for a booking (optional: if only one payment per booking)
    Optional<PaymentEntity> findByBookingId(String bookingId);

    // ✅ find by razorpay order id (useful during verify)
    Optional<PaymentEntity> findByRazorpayOrderId(String razorpayOrderId);
}