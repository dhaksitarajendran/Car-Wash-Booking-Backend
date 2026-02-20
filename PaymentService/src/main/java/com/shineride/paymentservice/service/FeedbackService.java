package com.shineride.paymentservice.service;

import com.shineride.paymentservice.entity.FeedbackEntity;

import java.util.List;

public interface FeedbackService {

    // ✅ CUSTOMER: create feedback only if booking COMPLETED
    FeedbackEntity createFeedback(String bookingId, FeedbackEntity feedback, String customerEmail);

    // ✅ CUSTOMER: update only own feedback
    FeedbackEntity updateMyFeedback(String feedbackId, FeedbackEntity updated, String customerEmail);

    // ✅ CUSTOMER: delete only own feedback (soft delete)
    void deleteMyFeedback(String feedbackId, String customerEmail);

    // ✅ CUSTOMER
    List<FeedbackEntity> getMyFeedbacks(String customerEmail);

    // ✅ ADMIN
    List<FeedbackEntity> getAllFeedbacks();

    // ✅ CUSTOMER optional filter
    List<FeedbackEntity> getMyFeedbacksByService(String customerEmail, String serviceId);
}