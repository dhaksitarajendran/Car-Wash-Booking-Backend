package com.shineride.bookingservice.repository;

import com.shineride.bookingservice.entity.ServiceProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceProgressRepository extends MongoRepository<ServiceProgress, String> {

    // get progress history in order
    List<ServiceProgress> findByBookingIdOrderByUpdatedAtAsc(String bookingId);
}