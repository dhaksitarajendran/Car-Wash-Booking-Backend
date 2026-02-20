package com.shineride.operationservice.repository;

import com.shineride.operationservice.entity.WashBayAssignmentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WashBayAssignmentRepository extends MongoRepository<WashBayAssignmentEntity, String> {

    // ✅ find assignment for a booking (1 booking -> 1 assignment)
    Optional<WashBayAssignmentEntity> findByBookingId(String bookingId);

    // ✅ employee view: only their assignments
    List<WashBayAssignmentEntity> findByEmployeeId(String employeeId);

    // ✅ to check capacity: count active assignments in a washbay
    long countByWashbayIdAndStatusIn(String washbayId, List<String> statuses);

    // ✅ system can find free slots: assignments that ended (bay is reusable)
    List<WashBayAssignmentEntity> findByWashbayIdAndEndTimeBefore(String washbayId, LocalDateTime time);
}