package com.shineride.paymentservice.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.shineride.paymentservice.entity.PaymentEntity;
import com.shineride.paymentservice.repository.PaymentRepository;
import com.shineride.paymentservice.service.PaymentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    private final RazorpayClient razorpayClient;

    @Value("${services.booking.base-url}")
    private String bookingBaseUrl;

    @Value("${services.coupon.base-url}")
    private String couponBaseUrl;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            RestTemplate restTemplate,
            @Value("${razorpay.key-id}") String razorpayKeyId,
            @Value("${razorpay.key-secret}") String razorpayKeySecret) throws Exception {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
        this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        this.razorpayKeySecret = razorpayKeySecret;
    }

    @Override
    public PaymentEntity createRazorpayOrder(String bookingId, String couponCode, String customerEmail) {

        if (bookingId == null || bookingId.trim().isEmpty())
            throw new RuntimeException("bookingId required");
        customerEmail = normEmail(customerEmail);

        // ✅ 1) Ask BookingService for initial amount AND ownership
        // Expected response example:
        // { "bookingId":"B1", "customerEmail":"a@x.com", "initialAmount": 850.0 }
        Map<String, Object> booking = restTemplate.getForObject(
                bookingBaseUrl + "/booking/internal/" + bookingId + "/payment-summary",
                Map.class);

        if (booking == null)
            throw new RuntimeException("Booking not found in BookingService");

        String owner = String.valueOf(booking.get("customerEmail")).toLowerCase();
        if (!owner.equals(customerEmail)) {
            throw new RuntimeException("Forbidden: booking does not belong to this customer");
        }

        Double initial = toDouble(booking.get("initialAmount"));
        if (initial == null || initial <= 0)
            throw new RuntimeException("Invalid initialAmount from BookingService");

        // ✅ 2) Apply coupon (optional)
        CouponResult couponResult = applyCouponIfEligible(couponCode, customerEmail, initial);

        double finalAmount = round2(initial - couponResult.discountAmount);
        if (finalAmount < 1)
            finalAmount = 1; // Razorpay min 1 INR

        // ✅ 3) Create Razorpay Order (amount in paise)
        JSONObject options = new JSONObject();
        options.put("amount", (int) Math.round(finalAmount * 100)); // paise
        options.put("currency", "INR");
        String shortBookingId = bookingId.length() > 8 ? bookingId.substring(bookingId.length() - 8) : bookingId;
        String shortTs = String.valueOf(System.currentTimeMillis()).substring(7); // last 6 digits
        options.put("receipt", "rcpt_" + shortBookingId + "_" + shortTs); // max ~22 chars

        Order order;
        try {
            order = razorpayClient.orders.create(options);
        } catch (Exception e) {
            throw new RuntimeException("Razorpay order create failed: " + e.getMessage());
        }

        // ✅ 4) Save payment record
        LocalDateTime now = LocalDateTime.now();

        PaymentEntity p = new PaymentEntity();
        p.setId(null);
        p.setBookingId(bookingId);
        p.setCustomerEmail(customerEmail);

        p.setInitialAmount(round2(initial));
        p.setCouponCode(couponResult.couponCode);
        p.setDiscountPercent(couponResult.discountPercent);
        p.setDiscountAmount(round2(couponResult.discountAmount));
        p.setFinalAmount(round2(finalAmount));

        p.setPaymentMethod("RAZORPAY");
        p.setPaymentStatus("CREATED");

        p.setRazorpayOrderId(order.get("id"));
        p.setTransactionRef(order.get("receipt"));

        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        return paymentRepository.save(p);
    }

    @Override
    public PaymentEntity verifyRazorpayPayment(
            String paymentId,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature,
            String customerEmail) {

        customerEmail = normEmail(customerEmail);

        PaymentEntity p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (!normEmail(p.getCustomerEmail()).equals(customerEmail)) {
            throw new RuntimeException("Forbidden: not your payment");
        }

        if (p.getRazorpayOrderId() == null || !p.getRazorpayOrderId().equals(razorpayOrderId)) {
            throw new RuntimeException("OrderId mismatch");
        }

        // ✅ Verify signature
        // signature = HMAC_SHA256(orderId + "|" + paymentId, key_secret)
        boolean ok;
        try {
            ok = Utils.verifyPaymentSignature(
                    new JSONObject(Map.of(
                            "razorpay_order_id", razorpayOrderId,
                            "razorpay_payment_id", razorpayPaymentId,
                            "razorpay_signature", razorpaySignature)),
                    razorpayKeySecret);
        } catch (Exception e) {
            throw new RuntimeException("Signature verify error: " + e.getMessage());
        }

        if (!ok) {
            p.setPaymentStatus("FAILED");
            p.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(p);
        }

        p.setRazorpayPaymentId(razorpayPaymentId);
        p.setRazorpaySignature(razorpaySignature);
        p.setPaymentStatus("PAID");
        p.setUpdatedAt(LocalDateTime.now());

        PaymentEntity saved = paymentRepository.save(p);

        // ✅ Notify BookingService that payment is PAID
        try {
            restTemplate.put(bookingBaseUrl + "/booking/internal/" + saved.getBookingId() + "/status?status=PAID",
                    null);
        } catch (Exception e) {
            System.err.println("Failed to notify BookingService about payment success for " + saved.getBookingId());
        }

        return saved;
    }

    @Override
    public List<PaymentEntity> getMyPayments(String customerEmail) {
        return paymentRepository.findByCustomerEmail(normEmail(customerEmail));
    }

    @Override
    public List<PaymentEntity> getAllPayments() {
        return paymentRepository.findAll();
    }

    // -----------------------
    // Coupon helper
    // -----------------------
    private CouponResult applyCouponIfEligible(String couponCode, String customerEmail, double initialAmount) {

        if (couponCode == null || couponCode.trim().isEmpty()) {
            return CouponResult.none();
        }

        couponCode = couponCode.trim().toUpperCase();

        // ✅ Call CouponService to validate coupon for this user and get discount info.
        // Expected response example:
        // { "couponCode":"NEW50", "type":"PERCENT", "percent":10, "flatAmount":0,
        // "minPurchase":500, "eligible":true }
        Map<String, Object> resp = restTemplate.getForObject(
                couponBaseUrl + "/coupon/internal/validate?email=" + customerEmail + "&code=" + couponCode + "&amount="
                        + initialAmount,
                Map.class);

        if (resp == null)
            return CouponResult.none();
        Boolean eligible = (Boolean) resp.get("eligible");
        if (eligible == null || !eligible)
            return CouponResult.none();

        Double minPurchase = toDouble(resp.get("minPurchase"));
        if (minPurchase != null && initialAmount < minPurchase)
            return CouponResult.none();

        String type = resp.get("type") == null ? "" : resp.get("type").toString().toUpperCase();

        double discount = 0;
        Double percent = toDouble(resp.get("percent"));
        Double flat = toDouble(resp.get("flatAmount"));

        if ("PERCENT".equals(type) && percent != null && percent > 0) {
            discount = initialAmount * (percent / 100.0);
        } else if ("FLAT".equals(type) && flat != null && flat > 0) {
            discount = flat;
        }

        discount = Math.min(discount, initialAmount);
        discount = round2(discount);

        return new CouponResult(couponCode, percent, discount);
    }

    private static String normEmail(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static Double toDouble(Object o) {
        if (o == null)
            return null;
        if (o instanceof Number n)
            return n.doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private record CouponResult(String couponCode, Double discountPercent, double discountAmount) {
        static CouponResult none() {
            return new CouponResult(null, null, 0);
        }
    }
}