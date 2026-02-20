package com.shineride.operationservice.service.impl;

import com.shineride.operationservice.dto.WashBayAssignmentRequestDTO;
import com.shineride.operationservice.dto.WashBayStatusUpdateDTO;
import com.shineride.operationservice.entity.WashBayAssignmentEntity;
import com.shineride.operationservice.entity.WashBayEntity;
import com.shineride.operationservice.repository.WashBayAssignmentRepository;
import com.shineride.operationservice.repository.WashBayRepository;
import com.shineride.operationservice.service.WashBayAssignmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WashBayAssignmentServiceImpl implements WashBayAssignmentService {

    private final WashBayAssignmentRepository assignmentRepository;
    private final WashBayRepository washBayRepository;

    // default slot duration (minutes) if you don't calculate by service type
    private final long slotMinutes;

    public WashBayAssignmentServiceImpl(
            WashBayAssignmentRepository assignmentRepository,
            WashBayRepository washBayRepository,
            @Value("${operation.slot-minutes:60}") long slotMinutes) {
        this.assignmentRepository = assignmentRepository;
        this.washBayRepository = washBayRepository;
        this.slotMinutes = slotMinutes;
    }

    // ✅ SYSTEM picks washbay automatically based on capacity
    @Override
    public WashBayAssignmentEntity assignWashBay(WashBayAssignmentRequestDTO dto) {

        if (dto == null)
            throw new RuntimeException("Body required");
        if (dto.getBookingId() == null || dto.getBookingId().trim().isEmpty())
            throw new RuntimeException("bookingId required");
        if (dto.getEmployeeId() == null || dto.getEmployeeId().trim().isEmpty())
            throw new RuntimeException("employeeId (email) required");

        String bookingId = dto.getBookingId().trim();
        String employeeEmail = dto.getEmployeeId().trim().toLowerCase();

        // one booking -> one assignment
        assignmentRepository.findByBookingId(bookingId).ifPresent(a -> {
            throw new RuntimeException("Booking already assigned to washbay. bookingId=" + bookingId);
        });

        // choose a bay with available capacity
        WashBayEntity selected = pickAvailableWashBay();
        if (selected == null) {
            throw new RuntimeException("No washbay available right now (all bays full)");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMinutes(slotMinutes);

        WashBayAssignmentEntity a = new WashBayAssignmentEntity();
        a.setId(null);
        a.setBookingId(bookingId);
        a.setWashbayId(selected.getId());
        a.setEmployeeId(employeeEmail);
        a.setStartTime(now);
        a.setEndTime(end);
        a.setStatus("ASSIGNED");
        a.setCreatedAt(now);
        a.setUpdatedAt(now);

        return assignmentRepository.save(a);
    }

    @Override
    public List<WashBayAssignmentEntity> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    @Override
    public List<WashBayAssignmentEntity> getAssignmentsByEmployee(String employeeEmail) {
        return assignmentRepository.findByEmployeeId(employeeEmail.toLowerCase().trim());
    }

    // ✅ EMPLOYEE can update ONLY their own assigned booking
    @Override
    public WashBayAssignmentEntity updateStatus(String assignmentId, WashBayStatusUpdateDTO dto, String employeeEmail) {

        if (dto == null || dto.getStatus() == null || dto.getStatus().trim().isEmpty()) {
            throw new RuntimeException("status required");
        }

        String newStatus = dto.getStatus().trim().toUpperCase();
        if (!List.of("ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED").contains(newStatus)) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        WashBayAssignmentEntity a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        String email = employeeEmail.toLowerCase().trim();

        // ✅ only assigned employee can update
        if (a.getEmployeeId() == null || !a.getEmployeeId().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden: not assigned to you");
        }

        a.setStatus(newStatus);
        a.setUpdatedAt(LocalDateTime.now());

        // ✅ when completed/cancelled, mark endTime now so bay becomes reusable
        // immediately
        if (newStatus.equals("COMPLETED") || newStatus.equals("CANCELLED")) {
            a.setEndTime(LocalDateTime.now());
        }

        return assignmentRepository.save(a);
    }

    // -----------------------
    // internal helper
    // -----------------------
    private WashBayEntity pickAvailableWashBay() {

        List<WashBayEntity> bays = washBayRepository.findByActiveTrue();
        if (bays.isEmpty())
            return null;

        // choose bay with most remaining slots (or any strategy)
        return bays.stream()
                .map(b -> new BayWithFreeSlots(b, freeSlotsFor(b)))
                .filter(x -> x.freeSlots > 0)
                .sorted(Comparator.comparingInt((BayWithFreeSlots x) -> x.freeSlots).reversed())
                .map(x -> x.bay)
                .findFirst()
                .orElse(null);
    }

    private int freeSlotsFor(WashBayEntity bay) {
        long activeCount = assignmentRepository.countByWashbayIdAndStatusIn(
                bay.getId(),
                List.of("ASSIGNED", "IN_PROGRESS"));
        int remaining = bay.getCapacity() - (int) activeCount;
        return Math.max(0, remaining);
    }

    @Override
    public WashBayAssignmentEntity getAssignmentByBooking(String bookingId) {
        return assignmentRepository.findByBookingId(bookingId).orElse(null);
    }

    private record BayWithFreeSlots(WashBayEntity bay, int freeSlots) {
    }
}