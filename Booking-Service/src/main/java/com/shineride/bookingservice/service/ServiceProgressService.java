package com.shineride.bookingservice.service;

import com.shineride.bookingservice.entity.ServiceProgress;
import com.shineride.bookingservice.entity.ServiceStage;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ServiceProgressService {

    List<ServiceProgress> getProgressForBooking(String bookingId, Authentication auth);

    ServiceProgress updateStage(String bookingId, ServiceStage stage, String updatedByEmail);
}