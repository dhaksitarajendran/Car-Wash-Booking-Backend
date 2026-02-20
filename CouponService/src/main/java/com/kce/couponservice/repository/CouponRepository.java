package com.kce.couponservice.repository;

import com.kce.couponservice.entity.CouponEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CouponRepository extends MongoRepository<CouponEntity, String> {

    Optional<CouponEntity> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String couponCode);
}