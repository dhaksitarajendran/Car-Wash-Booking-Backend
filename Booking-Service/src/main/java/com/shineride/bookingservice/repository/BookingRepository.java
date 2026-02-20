package com.shineride.bookingservice.repository;

import com.shineride.bookingservice.entity.BookingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<BookingEntity, String> {

    // CUSTOMER: fetch only their bookings
    List<BookingEntity> findByCustomerEmail(String customerEmail);
}