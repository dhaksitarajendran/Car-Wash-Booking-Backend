package com.shineride.paymentservice.repository;

import com.shineride.paymentservice.entity.FeedbackEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends MongoRepository<FeedbackEntity, String> {

    // ✅ user sees only their feedbacks
    List<FeedbackEntity> findByCustomerEmailAndActiveTrue(String customerEmail);

    // ✅ enforce one feedback per booking per customer (recommended)
    Optional<FeedbackEntity> findByBookingIdAndCustomerEmailAndActiveTrue(String bookingId, String customerEmail);

    // ✅ admin sees all active feedbacks
    List<FeedbackEntity> findByActiveTrue();

    // ✅ user filters by service (optional endpoint)
    List<FeedbackEntity> findByCustomerEmailAndServiceIdAndActiveTrue(String customerEmail, String serviceId);
}