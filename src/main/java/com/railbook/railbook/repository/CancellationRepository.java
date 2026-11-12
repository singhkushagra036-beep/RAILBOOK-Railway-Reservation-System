package com.railbook.railbook.repository;

import com.railbook.railbook.model.Booking;
import com.railbook.railbook.model.Cancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationRepository extends JpaRepository<Cancellation, Long> {
    Optional<Cancellation> findByBooking(Booking booking);
    List<Cancellation> findAllByOrderByCancellationDateDesc();
}
