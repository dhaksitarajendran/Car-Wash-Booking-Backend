package com.kce.couponservice.service.impl;

import com.kce.couponservice.dto.AssignCouponDTO;
import com.kce.couponservice.dto.CouponRequestDTO;
import com.kce.couponservice.dto.CouponValidateResponseDTO;
import com.kce.couponservice.entity.CouponAssignmentEntity;
import com.kce.couponservice.entity.CouponEntity;
import com.kce.couponservice.entity.DiscountType;
import com.kce.couponservice.repository.CouponAssignmentRepository;
import com.kce.couponservice.repository.CouponRepository;
import com.kce.couponservice.service.CouponService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponAssignmentRepository assignmentRepository;

    public CouponServiceImpl(CouponRepository couponRepository, CouponAssignmentRepository assignmentRepository) {
        this.couponRepository = couponRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // =========================
    // ADMIN
    // =========================

    @Override
    public CouponEntity createCoupon(CouponRequestDTO req) {

        CouponEntity c = new CouponEntity();

        String code = normCode(req.getCouponCode());
        if (code.isEmpty()) throw new RuntimeException("couponCode required");
        if (couponRepository.existsByCouponCode(code)) throw new RuntimeException("couponCode already exists");

        validateCouponRequest(req);

        c.setCouponCode(code);
        c.setDescription(req.getDescription());

        c.setDiscountType(req.getDiscountType());
        c.setDiscountPercent(req.getDiscountPercent());
        c.setFlatAmount(req.getFlatAmount());

        c.setMinOrderAmount(req.getMinOrderAmount());
        c.setValidFrom(req.getValidFrom());
        c.setValidUntil(req.getValidUntil());

        c.setActive(req.getActive() != null ? req.getActive() : true);

        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());

        try {
            return couponRepository.save(c);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("couponCode already exists");
        }
    }

    @Override
    public CouponEntity updateCoupon(String couponCode, CouponRequestDTO req) {

        String code = normCode(couponCode);

        CouponEntity existing = couponRepository.findByCouponCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + code));

        validateCouponRequest(req);

        // code cannot be changed via update (keep it stable)
        existing.setDescription(req.getDescription());

        existing.setDiscountType(req.getDiscountType());
        existing.setDiscountPercent(req.getDiscountPercent());
        existing.setFlatAmount(req.getFlatAmount());

        existing.setMinOrderAmount(req.getMinOrderAmount());
        existing.setValidFrom(req.getValidFrom());
        existing.setValidUntil(req.getValidUntil());

        if (req.getActive() != null) existing.setActive(req.getActive());

        existing.setUpdatedAt(LocalDateTime.now());

        return couponRepository.save(existing);
    }

    @Override
    public List<CouponAssignmentEntity> assignCouponToUsers(AssignCouponDTO dto) {

        String code = normCode(dto.getCouponCode());
        if (code.isEmpty()) throw new RuntimeException("couponCode required");

        CouponEntity coupon = couponRepository.findByCouponCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found: " + code));

        if (Boolean.FALSE.equals(coupon.getActive())) throw new RuntimeException("Coupon is inactive");

        if (dto.getUserEmails() == null || dto.getUserEmails().isEmpty()) {
            throw new RuntimeException("userEmails required");
        }

        List<CouponAssignmentEntity> saved = new ArrayList<>();

        for (String emailRaw : dto.getUserEmails()) {
            String email = normEmail(emailRaw);
            if (email.isEmpty()) continue;

            // Prevent duplicates per (couponCode + userEmail)
            if (assignmentRepository.findByCouponCodeAndUserEmail(code, email).isPresent()) {
                continue; // already assigned -> skip
            }

            CouponAssignmentEntity a = new CouponAssignmentEntity();
            a.setCouponId(coupon.getId());
            a.setCouponCode(code);
            a.setUserEmail(email);
            a.setUsed(false);
            a.setAssignedAt(LocalDateTime.now());

            try {
                saved.add(assignmentRepository.save(a));
            } catch (DuplicateKeyException e) {
                // race condition -> ignore
            }
        }

        return saved;
    }

    @Override
    public List<CouponEntity> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public List<CouponAssignmentEntity> getAssignmentsForCoupon(String couponCode) {
        return assignmentRepository.findByCouponCode(normCode(couponCode));
    }

    @Override
    public Map<String, Object> getCouponStats(String couponCode) {
        String code = normCode(couponCode);
        long assigned = assignmentRepository.countByCouponCode(code);
        long used = assignmentRepository.countByCouponCodeAndUsedTrue(code);

        return Map.of(
                "couponCode", code,
                "assignedCount", assigned,
                "usedCount", used,
                "unusedCount", Math.max(0, assigned - used)
        );
    }

    // =========================
    // CUSTOMER
    // =========================

    @Override
    public List<CouponAssignmentEntity> getMyCoupons(String userEmail) {
        return assignmentRepository.findByUserEmail(normEmail(userEmail));
    }

    // =========================
    // INTERNAL (PaymentService)
    // =========================

    @Override
    public CouponValidateResponseDTO validateCoupon(String userEmail, String couponCode, Double amount) {

        CouponValidateResponseDTO out = new CouponValidateResponseDTO();

        String email = normEmail(userEmail);
        String code = normCode(couponCode);

        if (email.isEmpty() || code.isEmpty() || amount == null) {
            out.setEligible(false);
            out.setReason("Missing email/code/amount");
            return out;
        }

        // must be assigned to user and unused
        CouponAssignmentEntity assignment = assignmentRepository.findByCouponCodeAndUserEmail(code, email)
                .orElse(null);

        if (assignment == null) {
            out.setEligible(false);
            out.setReason("Coupon not assigned to user");
            return out;
        }
        if (Boolean.TRUE.equals(assignment.getUsed())) {
            out.setEligible(false);
            out.setReason("Coupon already used");
            return out;
        }

        CouponEntity coupon = couponRepository.findByCouponCode(code)
                .orElse(null);

        if (coupon == null) {
            out.setEligible(false);
            out.setReason("Coupon not found");
            return out;
        }

        if (Boolean.FALSE.equals(coupon.getActive())) {
            out.setEligible(false);
            out.setReason("Coupon inactive");
            return out;
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            out.setEligible(false);
            out.setReason("Coupon not started yet");
            return out;
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            out.setEligible(false);
            out.setReason("Coupon expired");
            return out;
        }

        double min = coupon.getMinOrderAmount() == null ? 0 : coupon.getMinOrderAmount();
        if (amount < min) {
            out.setEligible(false);
            out.setReason("Amount below minimum order");
            out.setMinPurchase(min);
            return out;
        }

        out.setEligible(true);
        out.setCouponCode(code);
        out.setMinPurchase(min);

        DiscountType type = coupon.getDiscountType();
        out.setType(type == null ? null : type.name());

        if (type == DiscountType.PERCENT) {
            out.setPercent(coupon.getDiscountPercent());
            out.setFlatAmount(0.0);
        } else if (type == DiscountType.FLAT) {
            out.setFlatAmount(coupon.getFlatAmount());
            out.setPercent(0);
        }

        return out;
    }

    @Override
    public void markCouponUsed(String userEmail, String couponCode, String bookingId, String paymentId) {

        String email = normEmail(userEmail);
        String code = normCode(couponCode);

        CouponAssignmentEntity assignment = assignmentRepository.findByCouponCodeAndUserEmail(code, email)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (Boolean.TRUE.equals(assignment.getUsed())) return;

        assignment.setUsed(true);
        assignment.setUsedAt(LocalDateTime.now());
        assignment.setBookingId(bookingId);
        assignment.setPaymentId(paymentId);

        assignmentRepository.save(assignment);
    }

    // =========================
    // Auto cleanup (expired)
    // Deletes expired coupons + their assignments
    // Runs every hour
    // =========================
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();
        List<CouponEntity> all = couponRepository.findAll();

        for (CouponEntity c : all) {
            if (c.getValidUntil() != null && now.isAfter(c.getValidUntil())) {
                // delete assignments first
                List<CouponAssignmentEntity> assigns = assignmentRepository.findByCouponId(c.getId());
                if (!assigns.isEmpty()) assignmentRepository.deleteAll(assigns);

                // delete coupon master
                couponRepository.delete(c);
            }
        }
    }

    // =========================
    // validation helpers
    // =========================
    private void validateCouponRequest(CouponRequestDTO req) {

        if (req.getDescription() == null || req.getDescription().trim().isEmpty()) {
            throw new RuntimeException("description required");
        }
        if (req.getDiscountType() == null) throw new RuntimeException("discountType required");

        if (req.getMinOrderAmount() == null || req.getMinOrderAmount() < 0) {
            throw new RuntimeException("minOrderAmount required");
        }
        if (req.getValidFrom() == null || req.getValidUntil() == null) {
            throw new RuntimeException("validFrom and validUntil required");
        }
        if (!req.getValidUntil().isAfter(req.getValidFrom())) {
            throw new RuntimeException("validUntil must be after validFrom");
        }

        if (req.getDiscountType() == DiscountType.PERCENT) {
            if (req.getDiscountPercent() == null || req.getDiscountPercent() <= 0 || req.getDiscountPercent() > 100) {
                throw new RuntimeException("discountPercent must be 1..100");
            }
            req.setFlatAmount(null);
        } else if (req.getDiscountType() == DiscountType.FLAT) {
            if (req.getFlatAmount() == null || req.getFlatAmount() <= 0) {
                throw new RuntimeException("flatAmount must be > 0");
            }
            req.setDiscountPercent(null);
        }
    }

    private static String normEmail(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String normCode(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }
}