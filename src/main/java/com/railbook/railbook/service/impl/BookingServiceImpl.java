package com.railbook.railbook.service.impl;

import com.railbook.railbook.dto.BookingRequestDto;
import com.railbook.railbook.dto.PassengerDto;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.*;
import com.railbook.railbook.service.BookingService;
import com.railbook.railbook.service.SeatInventoryService;
import com.railbook.railbook.util.PnrGenerator;
import com.railbook.railbook.util.SeatAllocationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final TrainClassConfigRepository classConfigRepository;
    private final SeatInventoryService seatInventoryService;
    private final PnrGenerator pnrGenerator;
    private final SeatAllocationUtil seatAllocationUtil;
    private final PaymentRepository paymentRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              TrainRepository trainRepository,
                              TrainClassConfigRepository classConfigRepository,
                              SeatInventoryService seatInventoryService,
                              PnrGenerator pnrGenerator,
                              SeatAllocationUtil seatAllocationUtil,
                              PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.classConfigRepository = classConfigRepository;
        this.seatInventoryService = seatInventoryService;
        this.pnrGenerator = pnrGenerator;
        this.seatAllocationUtil = seatAllocationUtil;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Booking createBooking(String userEmail, BookingRequestDto requestDto) {
        if (requestDto.getPassengers() == null || requestDto.getPassengers().isEmpty()) {
            throw new BookingException("At least one passenger must be specified");
        }
        if (requestDto.getPassengers().size() > 6) {
            throw new BookingException("A maximum of 6 passengers can be booked per reservation");
        }

        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Train train = trainRepository.findById(requestDto.getTrainId())
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + requestDto.getTrainId()));

        LocalDate journeyDate = requestDto.getJourneyDate();
        if (journeyDate.isBefore(LocalDate.now())) {
            throw new BookingException("Journey date cannot be in the past");
        }

        TravelClass travelClass = requestDto.getTravelClass();
        TrainClassConfig classConfig = classConfigRepository.findByTrainAndTravelClass(train, travelClass)
                .orElseThrow(() -> new BookingException("Selected class " + travelClass.getCode() + " is not available on this train"));

        int passengerCount = requestDto.getPassengers().size();
        BigDecimal totalFare = classConfig.getBaseFare().multiply(BigDecimal.valueOf(passengerCount));

        // Reserve seats using pessimistic locking to prevent race condition/double-booking
        SeatInventory updatedInventory = seatInventoryService.reserveSeatsWithLock(train, journeyDate, travelClass, passengerCount);

        // Generate unique 10-digit PNR
        String pnr = pnrGenerator.generatePnr();
        while (bookingRepository.findByPnrNumber(pnr).isPresent()) {
            pnr = pnrGenerator.generatePnr();
        }

        Booking booking = new Booking(pnr, user, train, travelClass, journeyDate, totalFare);

        // Calculate slot starting index for seat allocation
        int startingSlotIndex = updatedInventory.getBookedSeats() - passengerCount;

        for (int i = 0; i < requestDto.getPassengers().size(); i++) {
            PassengerDto pDto = requestDto.getPassengers().get(i);
            Passenger passenger = new Passenger(
                    pDto.getFullName().trim(),
                    pDto.getAge(),
                    pDto.getGender(),
                    pDto.getBerthPreference()
            );

            SeatAllocationUtil.SeatAllocationResult seatResult = seatAllocationUtil.allocateSeat(
                    travelClass,
                    startingSlotIndex + i,
                    pDto.getBerthPreference()
            );

            passenger.setSeatNumber(seatResult.getSeatNumber());
            passenger.setAllocatedBerth(seatResult.getAllocatedBerth());

            booking.addPassenger(passenger);
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Process simulated payment
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment(
                txnId,
                savedBooking,
                totalFare,
                requestDto.getPaymentMode() != null ? requestDto.getPaymentMode() : PaymentMode.UPI,
                PaymentStatus.SUCCESS
        );
        paymentRepository.save(payment);
        savedBooking.setPayment(payment);

        return savedBooking;
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingByPnr(String pnr) {
        if (pnr == null || pnr.isBlank()) {
            throw new BookingException("PNR number cannot be empty");
        }
        return bookingRepository.findByPnrNumber(pnr.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No reservation found for PNR: " + pnr.trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        return bookingRepository.findByUserOrderByBookingDateDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookingDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalBookingCount() {
        return bookingRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getConfirmedBookingCount() {
        return bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCancelledBookingCount() {
        return bookingRepository.countByBookingStatus(BookingStatus.CANCELLED);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalConfirmedRevenue() {
        return bookingRepository.sumTotalConfirmedRevenue();
    }
}
