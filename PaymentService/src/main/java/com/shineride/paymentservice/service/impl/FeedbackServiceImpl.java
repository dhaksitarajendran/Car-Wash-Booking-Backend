package com.shineride.paymentservice.service.impl;

import com.shineride.paymentservice.entity.FeedbackEntity;
import com.shineride.paymentservice.repository.FeedbackRepository;
import com.shineride.paymentservice.service.FeedbackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RestTemplate restTemplate;

    @Value("${services.operation.base-url}")
    private String operationBaseUrl;

    @Value("${services.booking.base-url}")
    private String bookingBaseUrl;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository, RestTemplate restTemplate) {
        this.feedbackRepository = feedbackRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public FeedbackEntity createFeedback(String bookingId, FeedbackEntity feedback, String customerEmail) {

        if (bookingId == null || bookingId.trim().isEmpty()) throw new RuntimeException("bookingId required");
        if (feedback == null) throw new RuntimeException("body required");

        customerEmail = normEmail(customerEmail);

        // ✅ 1) Ensure booking belongs to customer + is COMPLETED
        ensureBookingBelongsToCustomer(bookingId, customerEmail);
        ensureBookingCompleted(bookingId);

        // ✅ 2) One feedback per booking per customer (recommended)
        feedbackRepository.findByBookingIdAndCustomerEmailAndActiveTrue(bookingId, customerEmail)
                .ifPresent(x -> { throw new RuntimeException("Feedback already exists for this booking"); });

        // ✅ 3) Validate rating
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new RuntimeException("rating must be 1..5");
        }

        LocalDateTime now = LocalDateTime.now();

        FeedbackEntity f = new FeedbackEntity();
        f.setId(null);
        f.setBookingId(bookingId);
        f.setCustomerEmail(customerEmail);

        // service context (optional but useful)
        f.setServiceId(feedback.getServiceId());
        f.setServiceName(feedback.getServiceName());

        f.setRating(feedback.getRating());
        f.setComment(feedback.getComment());

        f.setActive(true);
        f.setCreatedAt(now);
        f.setUpdatedAt(now);

        return feedbackRepository.save(f);
    }

    @Override
    public FeedbackEntity updateMyFeedback(String feedbackId, FeedbackEntity updated, String customerEmail) {

        customerEmail = normEmail(customerEmail);

        FeedbackEntity f = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + feedbackId));

        if (!f.getActive()) throw new RuntimeException("Feedback deleted");
        if (!normEmail(f.getCustomerEmail()).equals(customerEmail)) {
            throw new RuntimeException("Forbidden: not your feedback");
        }

        if (updated.getRating() != null) {
            if (updated.getRating() < 1 || updated.getRating() > 5) throw new RuntimeException("rating must be 1..5");
            f.setRating(updated.getRating());
        }
        if (updated.getComment() != null) {
            f.setComment(updated.getComment());
        }

        f.setUpdatedAt(LocalDateTime.now());
        return feedbackRepository.save(f);
    }

    @Override
    public void deleteMyFeedback(String feedbackId, String customerEmail) {

        customerEmail = normEmail(customerEmail);

        FeedbackEntity f = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found: " + feedbackId));

        if (!normEmail(f.getCustomerEmail()).equals(customerEmail)) {
            throw new RuntimeException("Forbidden: not your feedback");
        }

        // ✅ soft delete
        f.setActive(false);
        f.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(f);
    }

    @Override
    public List<FeedbackEntity> getMyFeedbacks(String customerEmail) {
        return feedbackRepository.findByCustomerEmailAndActiveTrue(normEmail(customerEmail));
    }

    @Override
    public List<FeedbackEntity> getAllFeedbacks() {
        return feedbackRepository.findByActiveTrue();
    }

    @Override
    public List<FeedbackEntity> getMyFeedbacksByService(String customerEmail, String serviceId) {
        return feedbackRepository.findByCustomerEmailAndServiceIdAndActiveTrue(normEmail(customerEmail), serviceId);
    }

    // -----------------------
    // Cross-service checks
    // -----------------------
    private void ensureBookingCompleted(String bookingId) {
        // Expected response example:
        // { "bookingId":"B1", "status":"COMPLETED" }
        Map<String, Object> resp = restTemplate.getForObject(
                operationBaseUrl + "/operation/internal/booking/" + bookingId + "/status",
                Map.class
        );

        if (resp == null || resp.get("status") == null) {
            throw new RuntimeException("Cannot verify booking status");
        }

        String status = resp.get("status").toString().toUpperCase();
        if (!"COMPLETED".equals(status)) {
            throw new RuntimeException("Booking not completed yet");
        }
    }

    private void ensureBookingBelongsToCustomer(String bookingId, String customerEmail) {
        // Expected response example:
        // { "bookingId":"B1", "customerEmail":"a@x.com" }
        Map<String, Object> resp = restTemplate.getForObject(
                bookingBaseUrl + "/booking/internal/" + bookingId + "/owner",
                Map.class
        );

        if (resp == null || resp.get("customerEmail") == null) {
            throw new RuntimeException("Cannot verify booking ownership");
        }

        String owner = resp.get("customerEmail").toString().toLowerCase();
        if (!owner.equals(customerEmail)) {
            throw new RuntimeException("Forbidden: booking does not belong to this customer");
        }
    }

    private static String normEmail(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}