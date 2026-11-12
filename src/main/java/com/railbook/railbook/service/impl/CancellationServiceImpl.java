package com.railbook.railbook.service.impl;

import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.BookingRepository;
import com.railbook.railbook.repository.CancellationRepository;
import com.railbook.railbook.repository.PaymentRepository;
import com.railbook.railbook.service.CancellationService;
import com.railbook.railbook.service.SeatInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CancellationServiceImpl implements CancellationService {

    private final BookingRepository bookingRepository;
    private final CancellationRepository cancellationRepository;
    private final SeatInventoryService seatInventoryService;
    private final PaymentRepository paymentRepository;

    public CancellationServiceImpl(BookingRepository bookingRepository,
                                   CancellationRepository cancellationRepository,
                                   SeatInventoryService seatInventoryService,
                                   PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.cancellationRepository = cancellationRepository;
        this.seatInventoryService = seatInventoryService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Cancellation cancelTicket(String pnr, String userEmail, String cancellationReason) {
        Booking booking = bookingRepository.findByPnrNumber(pnr.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with PNR: " + pnr));

        // Security check: ensure requesting user owns the booking or is admin
        if (userEmail != null && !booking.getUser().getEmail().equalsIgnoreCase(userEmail.trim())
                && !booking.getUser().getRole().name().equals("ROLE_ADMIN")) {
            // Check if user is admin
            boolean isOwner = booking.getUser().getEmail().equalsIgnoreCase(userEmail.trim());
            if (!isOwner) {
                throw new BookingException("You are not authorized to cancel this booking");
            }
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("This ticket is already cancelled");
        }

        BigDecimal refundAmount = calculateSimulatedRefund(booking);
        BigDecimal cancellationCharge = calculateCancellationCharges(booking);

        // 1. Update Booking status
        booking.setBookingStatus(BookingStatus.CANCELLED);

        // 2. Restore seat inventory
        seatInventoryService.restoreSeats(
                booking.getTrain(),
                booking.getJourneyDate(),
                booking.getTravelClass(),
                booking.getPassengers().size()
        );

        // 3. Update payment status if exists
        Payment payment = booking.getPayment();
        if (payment != null) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        // 4. Create Cancellation entity
        Cancellation cancellation = new Cancellation(
                booking,
                refundAmount,
                cancellationCharge,
                cancellationReason != null ? cancellationReason : "User requested cancellation"
        );
        cancellation.setRefundStatus("SIMULATED_PROCESSED");

        Cancellation saved = cancellationRepository.save(cancellation);
        booking.setCancellation(saved);
        bookingRepository.save(booking);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cancellation> getAllCancellations() {
        return cancellationRepository.findAllByOrderByCancellationDateDesc();
    }

    @Override
    public BigDecimal calculateSimulatedRefund(Booking booking) {
        LocalDateTime departureTime = LocalDateTime.of(booking.getJourneyDate(), booking.getTrain().getDepartureTime());
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(departureTime)) {
            return BigDecimal.ZERO; // No refund after train departure
        }

        long hoursToDeparture = Duration.between(now, departureTime).toHours();
        BigDecimal totalFare = booking.getTotalFare();

        double refundPercentage;
        if (hoursToDeparture >= 48) {
            refundPercentage = 0.80; // 80% refund (20% clerkage fee)
        } else if (hoursToDeparture >= 24) {
            refundPercentage = 0.75; // 75% refund
        } else if (hoursToDeparture >= 12) {
            refundPercentage = 0.50; // 50% refund
        } else {
            refundPercentage = 0.25; // 25% refund
        }

        return totalFare.multiply(BigDecimal.valueOf(refundPercentage)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateCancellationCharges(Booking booking) {
        BigDecimal refund = calculateSimulatedRefund(booking);
        return booking.getTotalFare().subtract(refund).setScale(2, RoundingMode.HALF_UP);
    }
}
