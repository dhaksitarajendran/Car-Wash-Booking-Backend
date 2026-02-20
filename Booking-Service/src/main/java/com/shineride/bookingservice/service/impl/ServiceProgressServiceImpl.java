package com.shineride.bookingservice.service.impl;

import com.shineride.bookingservice.entity.ServiceProgress;
import com.shineride.bookingservice.entity.ServiceStage;
import com.shineride.bookingservice.repository.ServiceProgressRepository;
import com.shineride.bookingservice.service.BookingService;
import com.shineride.bookingservice.service.ServiceProgressService;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceProgressServiceImpl implements ServiceProgressService {

    private final ServiceProgressRepository serviceProgressRepository;
    private final BookingService bookingService;

    public ServiceProgressServiceImpl(ServiceProgressRepository serviceProgressRepository,
                                     BookingService bookingService) {
        this.serviceProgressRepository = serviceProgressRepository;
        this.bookingService = bookingService;
    }

    @Override
    public List<ServiceProgress> getProgressForBooking(String bookingId, Authentication auth) {

        boolean isCustomer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        if (isCustomer) {
            bookingService.assertOwner(bookingId, auth.getName());
        }

        return serviceProgressRepository.findByBookingIdOrderByUpdatedAtAsc(bookingId);
    }

    @Override
    public ServiceProgress updateStage(String bookingId, ServiceStage stage, String updatedByEmail) {

        ServiceProgress progress = new ServiceProgress();
        progress.setBookingId(bookingId);
        progress.setCurrentStage(stage);
        progress.setUpdatedBy(updatedByEmail);
        progress.setUpdatedAt(LocalDateTime.now());

        return serviceProgressRepository.save(progress);
    }
}