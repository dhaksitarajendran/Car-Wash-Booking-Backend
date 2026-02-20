package com.shineride.bookingservice.controller;

import com.shineride.bookingservice.entity.BookingEntity;
import com.shineride.bookingservice.repository.BookingRepository;
import com.shineride.bookingservice.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    // ✅ CUSTOMER + ADMIN can create booking
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public BookingEntity createBooking(@RequestBody BookingEntity booking, Authentication auth) {
        return bookingService.createBooking(booking, auth.getName());
    }

    // ✅ CUSTOMER can see only their bookings
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<BookingEntity> myBookings(Authentication auth) {
        return bookingService.getMyBookings(auth.getName());
    }

    // ✅ CUSTOMER can delete only their booking
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String deleteMyBooking(@PathVariable String bookingId, Authentication auth) {
        bookingService.deleteMyBooking(bookingId, auth.getName());
        return "Deleted bookingId = " + bookingId;
    }

    // ✅ ADMIN can see all bookings
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingEntity> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // ✅ ADMIN can edit any customer's booking
    @PutMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingEntity adminUpdateBooking(@PathVariable String bookingId, @RequestBody BookingEntity updated) {
        return bookingService.adminUpdateBooking(bookingId, updated);
    }

    // ✅ INTERNAL: Called by PaymentService to get booking summary for order
    // creation
    // No @PreAuthorize — this is a service-to-service internal call
    @GetMapping("/internal/{bookingId}/payment-summary")
    public Map<String, Object> getPaymentSummary(@PathVariable String bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        Map<String, Object> summary = new HashMap<>();
        summary.put("bookingId", booking.getId());
        summary.put("customerEmail", booking.getCustomerEmail());
        summary.put("initialAmount", booking.getTotalCost() != null ? booking.getTotalCost() : 0.0);
        summary.put("serviceId", booking.getServiceId());
        summary.put("status", booking.getStatus());
        summary.put("address", booking.getAddress());
        return summary;
    }

    // ✅ INTERNAL: Called by PaymentService when payment is successful
    @PutMapping("/internal/{bookingId}/status")
    public void updateStatus(@PathVariable String bookingId, @RequestParam String status) {
        bookingService.updateStatusInternal(bookingId, status);
    }
}
