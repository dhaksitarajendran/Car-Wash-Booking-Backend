package com.shineride.paymentservice.controller;

import com.shineride.paymentservice.entity.FeedbackEntity;
import com.shineride.paymentservice.entity.PaymentEntity;
import com.shineride.paymentservice.service.FeedbackService;
import com.shineride.paymentservice.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final FeedbackService feedbackService;

    public PaymentController(PaymentService paymentService, FeedbackService feedbackService) {
        this.paymentService = paymentService;
        this.feedbackService = feedbackService;
    }

    // ==========================
    // ✅ PAYMENTS (RAZORPAY)
    // ==========================

    // CUSTOMER: create a Razorpay order for a booking (coupon optional)
    // Body can include couponCode (optional). initialAmount should be computed from booking details in service.
    @PostMapping("/razorpay/order/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentEntity createRazorpayOrder(
            @PathVariable String bookingId,
            @RequestParam(required = false) String couponCode,
            Authentication authentication
    ) {
        String customerEmail = authentication.getName();
        return paymentService.createRazorpayOrder(bookingId, couponCode, customerEmail);
    }

    // CUSTOMER: verify payment after Razorpay success
    @PostMapping("/razorpay/verify/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentEntity verifyRazorpayPayment(
            @PathVariable String paymentId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpaySignature,
            Authentication authentication
    ) {
        String customerEmail = authentication.getName();
        return paymentService.verifyRazorpayPayment(paymentId, razorpayPaymentId, razorpayOrderId, razorpaySignature, customerEmail);
    }

    // CUSTOMER: view only my payments
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<PaymentEntity> myPayments(Authentication authentication) {
        return paymentService.getMyPayments(authentication.getName());
    }

    // ADMIN: view all payments
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentEntity> allPayments() {
        return paymentService.getAllPayments();
    }

    // ==========================
    // ✅ FEEDBACK
    // ==========================

    // CUSTOMER: create feedback for a completed booking
    @PostMapping("/feedback/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public FeedbackEntity createFeedback(
            @PathVariable String bookingId,
            @RequestBody FeedbackEntity feedback,
            Authentication authentication
    ) {
        String customerEmail = authentication.getName();
        return feedbackService.createFeedback(bookingId, feedback, customerEmail);
    }

    // CUSTOMER: update my feedback
    @PutMapping("/feedback/{feedbackId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public FeedbackEntity updateMyFeedback(
            @PathVariable String feedbackId,
            @RequestBody FeedbackEntity updated,
            Authentication authentication
    ) {
        String customerEmail = authentication.getName();
        return feedbackService.updateMyFeedback(feedbackId, updated, customerEmail);
    }

    // CUSTOMER: delete my feedback (soft delete)
    @DeleteMapping("/feedback/{feedbackId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteMyFeedback(
            @PathVariable String feedbackId,
            Authentication authentication
    ) {
        String customerEmail = authentication.getName();
        feedbackService.deleteMyFeedback(feedbackId, customerEmail);
        return "Feedback deleted";
    }

    // CUSTOMER: view only my feedbacks
    @GetMapping("/feedback/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<FeedbackEntity> myFeedbacks(Authentication authentication) {
        return feedbackService.getMyFeedbacks(authentication.getName());
    }

    // ADMIN: view all feedbacks
    @GetMapping("/feedback/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FeedbackEntity> allFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }
}