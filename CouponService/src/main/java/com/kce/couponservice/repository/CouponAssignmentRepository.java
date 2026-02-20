package com.kce.couponservice.repository;

import com.kce.couponservice.entity.CouponAssignmentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CouponAssignmentRepository extends MongoRepository<CouponAssignmentEntity, String> {

    List<CouponAssignmentEntity> findByUserEmail(String userEmail);

    List<CouponAssignmentEntity> findByCouponCode(String couponCode);

    long countByCouponCode(String couponCode);

    long countByCouponCodeAndUsedTrue(String couponCode);

    Optional<CouponAssignmentEntity> findByCouponCodeAndUserEmail(String couponCode, String userEmail);

    List<CouponAssignmentEntity> findByCouponId(String couponId);
}