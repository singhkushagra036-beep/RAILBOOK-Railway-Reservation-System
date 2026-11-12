package com.railbook.railbook.repository;

import com.railbook.railbook.model.Booking;
import com.railbook.railbook.model.BookingStatus;
import com.railbook.railbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnrNumber(String pnrNumber);

    List<Booking> findByUserOrderByBookingDateDesc(User user);

    List<Booking> findAllByOrderByBookingDateDesc();

    long countByBookingStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalFare), 0) FROM Booking b WHERE b.bookingStatus = 'CONFIRMED'")
    BigDecimal sumTotalConfirmedRevenue();
}
