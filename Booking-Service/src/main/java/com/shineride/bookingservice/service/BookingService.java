package com.shineride.bookingservice.service;

import com.shineride.bookingservice.entity.BookingEntity;
import java.util.List;

public interface BookingService {

    BookingEntity createBooking(BookingEntity booking, String loggedInEmail);

    List<BookingEntity> getMyBookings(String loggedInEmail);

    List<BookingEntity> getAllBookings();

    void deleteMyBooking(String bookingId, String loggedInEmail);

    BookingEntity adminUpdateBooking(String bookingId, BookingEntity updatedBooking);

    void assertOwner(String bookingId, String loggedInEmail);

    void updateStatusInternal(String bookingId, String status);
}