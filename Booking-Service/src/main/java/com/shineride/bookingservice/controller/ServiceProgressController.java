package com.shineride.bookingservice.controller;

import com.shineride.bookingservice.entity.ServiceProgress;
import com.shineride.bookingservice.entity.ServiceStage;
import com.shineride.bookingservice.service.ServiceProgressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/progress")
public class ServiceProgressController {

    private final ServiceProgressService serviceProgressService;

    public ServiceProgressController(ServiceProgressService serviceProgressService) {
        this.serviceProgressService = serviceProgressService;
    }

    // ✅ CUSTOMER can view progress of their own booking (ownership check should be done in service)
    // ✅ ADMIN/EMPLOYEE can view any booking progress
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','EMPLOYEE')")
    public List<ServiceProgress> getProgress(@PathVariable String bookingId, Authentication auth) {
        return serviceProgressService.getProgressForBooking(bookingId, auth);
    }

    // ✅ ONLY ADMIN/EMPLOYEE can update progress stage
    @PutMapping("/booking/{bookingId}/stage")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ServiceProgress updateStage(
            @PathVariable String bookingId,
            @RequestParam ServiceStage stage,
            Authentication auth
    ) {
        return serviceProgressService.updateStage(bookingId, stage, auth.getName());
    }
}