package com.shineride.bookingservice.service.impl;

import com.shineride.bookingservice.entity.BookingEntity;
import com.shineride.bookingservice.repository.BookingRepository;
import com.shineride.bookingservice.service.BookingService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingEntity createBooking(BookingEntity booking, String loggedInEmail) {
        booking.setCustomerEmail(loggedInEmail.toLowerCase().trim());
        booking.setId(null); // let Mongo generate id
        return bookingRepository.save(booking);
    }

    @Override
    public List<BookingEntity> getMyBookings(String loggedInEmail) {
        return bookingRepository.findByCustomerEmail(loggedInEmail.toLowerCase().trim());
    }

    @Override
    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public void deleteMyBooking(String bookingId, String loggedInEmail) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomerEmail().equalsIgnoreCase(loggedInEmail)) {
            throw new AccessDeniedException("You are not allowed to delete this booking");
        }

        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookingEntity adminUpdateBooking(String bookingId, BookingEntity updatedBooking) {
        BookingEntity existing = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // update fields (customerEmail should NOT be changed)
        existing.setServiceId(updatedBooking.getServiceId());
        existing.setAddOnIds(updatedBooking.getAddOnIds());
        existing.setEmployeeId(updatedBooking.getEmployeeId());
        existing.setTotalCost(updatedBooking.getTotalCost() != null ? updatedBooking.getTotalCost() : 0.0);
        existing.setUserId(updatedBooking.getUserId());
        existing.setAddress(updatedBooking.getAddress());
        existing.setPickupDate(updatedBooking.getPickupDate());
        existing.setDropDate(updatedBooking.getDropDate());

        return bookingRepository.save(existing);
    }

    @Override
    public void assertOwner(String bookingId, String loggedInEmail) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomerEmail().equalsIgnoreCase(loggedInEmail)) {
            throw new AccessDeniedException("Access denied: Not your booking");
        }
    }

    @Override
    public void updateStatusInternal(String bookingId, String status) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setStatus(status);
        bookingRepository.save(booking);
    }
}