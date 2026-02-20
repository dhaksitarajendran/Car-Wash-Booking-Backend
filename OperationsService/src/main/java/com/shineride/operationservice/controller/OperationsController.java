package com.shineride.operationservice.controller;

import com.shineride.operationservice.dto.WashBayAssignmentRequestDTO;
import com.shineride.operationservice.dto.WashBayRequestDTO;
import com.shineride.operationservice.dto.WashBayStatusUpdateDTO;
import com.shineride.operationservice.entity.WashBayAssignmentEntity;
import com.shineride.operationservice.entity.WashBayEntity;
import com.shineride.operationservice.service.WashBayAssignmentService;
import com.shineride.operationservice.service.WashBayService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operation")
public class OperationsController {

    private final WashBayService washBayService;
    private final WashBayAssignmentService assignmentService;

    public OperationsController(WashBayService washBayService, WashBayAssignmentService assignmentService) {
        this.washBayService = washBayService;
        this.assignmentService = assignmentService;
    }

    // =========================
    // ✅ WASHBAY (ADMIN ONLY)
    // =========================

    @PostMapping("/washbays")
    @PreAuthorize("hasRole('ADMIN')")
    public WashBayEntity createWashBay(@RequestBody WashBayRequestDTO dto) {
        return washBayService.createWashBay(dto);
    }

    @GetMapping("/washbays")
    @PreAuthorize("hasRole('ADMIN')")
    public List<WashBayEntity> getAllWashBays() {
        return washBayService.getAllWashBays();
    }

    @PutMapping("/washbays/{washbayId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public WashBayEntity disableWashBay(@PathVariable String washbayId) {
        return washBayService.disableWashBay(washbayId);
    }

    // =========================
    // ✅ ASSIGNMENTS
    // =========================

    // ✅ ADMIN: auto assign booking -> washbay
    @PostMapping("/assignments/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public WashBayAssignmentEntity assignWashBay(@RequestBody WashBayAssignmentRequestDTO dto) {
        return assignmentService.assignWashBay(dto);
    }

    // ✅ ADMIN: view all assignments
    @GetMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public List<WashBayAssignmentEntity> getAllAssignments() {
        return assignmentService.getAllAssignments();
    }

    // ✅ EMPLOYEE: view only their assignments
    @GetMapping("/assignments/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<WashBayAssignmentEntity> myAssignments(Authentication authentication) {
        return assignmentService.getAssignmentsByEmployee(authentication.getName());
    }

    // ✅ EMPLOYEE: update status ONLY if assigned to that booking
    @PutMapping("/assignments/{assignmentId}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public WashBayAssignmentEntity updateStatus(
            @PathVariable String assignmentId,
            @RequestBody WashBayStatusUpdateDTO dto,
            Authentication authentication) {
        return assignmentService.updateStatus(assignmentId, dto, authentication.getName());
    }

    @GetMapping("/assignments/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','EMPLOYEE')")
    public WashBayAssignmentEntity getAssignmentByBooking(@PathVariable String bookingId) {
        return assignmentService.getAssignmentByBooking(bookingId);
    }
}