package com.railbook.railbook.service;

import com.railbook.railbook.dto.BookingRequestDto;
import com.railbook.railbook.model.Booking;

import java.math.BigDecimal;
import java.util.List;

public interface BookingService {
    Booking createBooking(String userEmail, BookingRequestDto requestDto);
    Booking getBookingByPnr(String pnr);
    List<Booking> getUserBookings(String userEmail);
    List<Booking> getAllBookings();
    long getTotalBookingCount();
    long getConfirmedBookingCount();
    long getCancelledBookingCount();
    BigDecimal getTotalConfirmedRevenue();
}
