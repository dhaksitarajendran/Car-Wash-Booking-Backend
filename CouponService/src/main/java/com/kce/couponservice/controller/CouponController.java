package com.kce.couponservice.controller;

import com.kce.couponservice.dto.AssignCouponDTO;
import com.kce.couponservice.dto.CouponRequestDTO;
import com.kce.couponservice.dto.CouponValidateResponseDTO;
import com.kce.couponservice.entity.CouponAssignmentEntity;
import com.kce.couponservice.entity.CouponEntity;
import com.kce.couponservice.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // ==========================
    // ✅ ADMIN ONLY
    // ==========================

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public CouponEntity createCoupon(@RequestBody CouponRequestDTO req) {
        return couponService.createCoupon(req);
    }

    @PutMapping("/admin/{couponCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public CouponEntity updateCoupon(@PathVariable String couponCode, @RequestBody CouponRequestDTO req) {
        return couponService.updateCoupon(couponCode, req);
    }

    // assign same coupon to many users
    @PostMapping("/admin/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CouponAssignmentEntity> assignCoupon(@RequestBody AssignCouponDTO dto) {
        return couponService.assignCouponToUsers(dto);
    }

    // admin view all coupons
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CouponEntity> allCoupons() {
        return couponService.getAllCoupons();
    }

    // admin view assignments for a coupon code
    @GetMapping("/admin/{couponCode}/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CouponAssignmentEntity> assignments(@PathVariable String couponCode) {
        return couponService.getAssignmentsForCoupon(couponCode);
    }

    // admin stats: how many assigned + how many used
    @GetMapping("/admin/{couponCode}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> stats(@PathVariable String couponCode) {
        return ResponseEntity.ok(couponService.getCouponStats(couponCode));
    }

    // ==========================
    // ✅ CUSTOMER ONLY
    // ==========================

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<CouponAssignmentEntity> myCoupons(Authentication auth) {
        return couponService.getMyCoupons(auth.getName());
    }

    // ==========================
    // ✅ INTERNAL (PaymentService calls these)
    // Safest: ADMIN only in your system OR allow authenticated internal gateway.
    // If you want: restrict to ADMIN (recommended).
    // ==========================

    // PaymentService calls this to validate coupon
    // /coupon/internal/validate?email=a@b.com&code=NEW10&amount=850
    @GetMapping("/internal/validate")
    @PreAuthorize("hasRole('ADMIN')") // safest (PaymentService can call with ADMIN token)
    public CouponValidateResponseDTO validate(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam Double amount
    ) {
        return couponService.validateCoupon(email, code, amount);
    }

    // PaymentService calls this after payment success to mark coupon used
    @PostMapping("/internal/mark-used")
    @PreAuthorize("hasRole('ADMIN')") // safest (PaymentService calls with ADMIN token)
    public ResponseEntity<?> markUsed(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String bookingId,
            @RequestParam String paymentId
    ) {
        couponService.markCouponUsed(email, code, bookingId, paymentId);
        return ResponseEntity.ok(Map.of("status", "MARKED_USED"));
    }
}