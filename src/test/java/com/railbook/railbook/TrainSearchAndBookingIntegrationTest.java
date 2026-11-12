package com.railbook.railbook;

import com.railbook.railbook.dto.BookingRequestDto;
import com.railbook.railbook.dto.PassengerDto;
import com.railbook.railbook.dto.TrainSearchDto;
import com.railbook.railbook.dto.TrainSearchResultDto;
import com.railbook.railbook.exception.SeatNotAvailableException;
import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.BookingRepository;
import com.railbook.railbook.repository.TrainRepository;
import com.railbook.railbook.service.BookingService;
import com.railbook.railbook.service.CancellationService;
import com.railbook.railbook.service.SeatInventoryService;
import com.railbook.railbook.service.TrainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TrainSearchAndBookingIntegrationTest {

    @Autowired
    private TrainService trainService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CancellationService cancellationService;

    @Autowired
    private SeatInventoryService seatInventoryService;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Should search trains between New Delhi and Lucknow and verify class availability")
    void testTrainSearch() {
        TrainSearchDto searchDto = new TrainSearchDto();
        searchDto.setSourceStationCode("NDLS");
        searchDto.setDestinationStationCode("LKO");
        searchDto.setJourneyDate(LocalDate.now().plusDays(2));

        List<TrainSearchResultDto> results = trainService.searchTrains(searchDto);

        assertNotNull(results);
        assertFalse(results.isEmpty(), "Expected at least 1 train between NDLS and LKO");

        TrainSearchResultDto shatabdi = results.stream()
                .filter(t -> t.getTrainNumber().equals("12004"))
                .findFirst()
                .orElse(null);

        assertNotNull(shatabdi, "Expected train 12004 (Lucknow Swarna Shatabdi Express)");
        assertEquals("New Delhi", shatabdi.getSourceStationName());
        assertEquals("Lucknow Charbagh", shatabdi.getDestinationStationName());
        assertFalse(shatabdi.getClassAvailabilities().isEmpty());

        // Verify CC class is present and available
        TrainSearchResultDto.ClassAvailabilityDto cc = shatabdi.getClassAvailabilities().stream()
                .filter(c -> c.getClassCode().equals("CC"))
                .findFirst()
                .orElse(null);

        assertNotNull(cc);
        assertTrue(cc.getAvailableSeats() > 0);
        assertTrue(cc.getFare().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should successfully book ticket with multiple passengers, generate 10-digit PNR and allocate seats")
    void testTicketBookingFlow() {
        Train train = trainRepository.findByTrainNumber("12004")
                .orElseThrow(() -> new IllegalStateException("Train 12004 not found"));

        LocalDate journeyDate = LocalDate.now().plusDays(3);
        TravelClass travelClass = TravelClass.AC_CHAIR_CAR;

        int initialAvailable = seatInventoryService.getAvailableSeats(train, journeyDate, travelClass);

        // Prepare 2 passengers
        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setTrainId(train.getId());
        bookingRequest.setTravelClass(travelClass);
        bookingRequest.setJourneyDate(journeyDate);
        bookingRequest.setPaymentMode(PaymentMode.UPI);

        List<PassengerDto> passengers = new ArrayList<>();
        passengers.add(new PassengerDto("Rohan Verma", 29, "MALE", BerthPreference.WINDOW));
        passengers.add(new PassengerDto("Priya Verma", 27, "FEMALE", BerthPreference.NO_PREFERENCE));
        bookingRequest.setPassengers(passengers);

        // Execute Booking
        Booking booking = bookingService.createBooking("user@railbook.com", bookingRequest);

        assertNotNull(booking);
        assertNotNull(booking.getId());

        // Verify 10-digit numeric PNR
        assertNotNull(booking.getPnrNumber());
        assertEquals(10, booking.getPnrNumber().length(), "PNR must be exactly 10 digits");
        assertTrue(booking.getPnrNumber().matches("\\d{10}"), "PNR must be numeric");

        // Verify Status & Fare
        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        assertEquals(2, booking.getPassengers().size());
        assertTrue(booking.getTotalFare().compareTo(BigDecimal.ZERO) > 0);

        // Verify Simulated Payment
        assertNotNull(booking.getPayment());
        assertEquals(PaymentStatus.SUCCESS, booking.getPayment().getPaymentStatus());
        assertTrue(booking.getPayment().getTransactionId().startsWith("TXN-"));

        // Verify Seat Allocation
        Passenger p1 = booking.getPassengers().get(0);
        assertNotNull(p1.getSeatNumber(), "Passenger 1 must have an allocated seat number");
        assertTrue(p1.getSeatNumber().startsWith("C1-"));

        // Verify Seat Inventory deduction
        int updatedAvailable = seatInventoryService.getAvailableSeats(train, journeyDate, travelClass);
        assertEquals(initialAvailable - 2, updatedAvailable);
    }

    @Test
    @DisplayName("Should retrieve booking status via PNR")
    void testPnrStatusRetrieval() {
        Train train = trainRepository.findByTrainNumber("22436")
                .orElseThrow(() -> new IllegalStateException("Train 22436 not found"));

        LocalDate journeyDate = LocalDate.now().plusDays(4);

        BookingRequestDto req = new BookingRequestDto();
        req.setTrainId(train.getId());
        req.setTravelClass(TravelClass.AC_CHAIR_CAR);
        req.setJourneyDate(journeyDate);
        req.setPaymentMode(PaymentMode.CREDIT_CARD);

        List<PassengerDto> passengers = List.of(new PassengerDto("Suresh Kumar", 45, "MALE", BerthPreference.WINDOW));
        req.setPassengers(passengers);

        Booking created = bookingService.createBooking("user@railbook.com", req);
        String pnr = created.getPnrNumber();

        // Retrieve by PNR
        Booking fetched = bookingService.getBookingByPnr(pnr);
        assertNotNull(fetched);
        assertEquals(pnr, fetched.getPnrNumber());
        assertEquals(train.getTrainNumber(), fetched.getTrain().getTrainNumber());
        assertEquals(1, fetched.getPassengers().size());
    }

    @Test
    @DisplayName("Should cancel ticket, calculate simulated refund and restore seats back to inventory")
    void testTicketCancellationAndSeatRestoration() {
        Train train = trainRepository.findByTrainNumber("12004")
                .orElseThrow(() -> new IllegalStateException("Train 12004 not found"));

        LocalDate journeyDate = LocalDate.now().plusDays(5);
        TravelClass travelClass = TravelClass.AC_CHAIR_CAR;

        int seatsBeforeBooking = seatInventoryService.getAvailableSeats(train, journeyDate, travelClass);

        BookingRequestDto req = new BookingRequestDto();
        req.setTrainId(train.getId());
        req.setTravelClass(travelClass);
        req.setJourneyDate(journeyDate);
        req.setPaymentMode(PaymentMode.DEBIT_CARD);

        List<PassengerDto> passengers = List.of(new PassengerDto("Vikram Singh", 35, "MALE", BerthPreference.NO_PREFERENCE));
        req.setPassengers(passengers);

        Booking booking = bookingService.createBooking("user@railbook.com", req);
        String pnr = booking.getPnrNumber();

        int seatsAfterBooking = seatInventoryService.getAvailableSeats(train, journeyDate, travelClass);
        assertEquals(seatsBeforeBooking - 1, seatsAfterBooking);

        // Cancel Ticket
        Cancellation cancellation = cancellationService.cancelTicket(pnr, "user@railbook.com", "Travel rescheduled");

        assertNotNull(cancellation);
        assertTrue(cancellation.getRefundAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(cancellation.getCancellationCharge().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(booking.getTotalFare(), cancellation.getRefundAmount().add(cancellation.getCancellationCharge()));

        // Verify booking status is CANCELLED
        Booking cancelledBooking = bookingService.getBookingByPnr(pnr);
        assertEquals(BookingStatus.CANCELLED, cancelledBooking.getBookingStatus());

        // Verify Seat Inventory was restored back to original available count!
        int seatsAfterCancellation = seatInventoryService.getAvailableSeats(train, journeyDate, travelClass);
        assertEquals(seatsBeforeBooking, seatsAfterCancellation);
    }

    @Test
    @DisplayName("Should prevent overbooking when seats are unavailable")
    void testOverbookingPrevention() {
        Train train = trainRepository.findByTrainNumber("12952")
                .orElseThrow(() -> new IllegalStateException("Train 12952 not found"));

        LocalDate journeyDate = LocalDate.now().plusDays(6);
        TravelClass travelClass = TravelClass.FIRST_AC;

        SeatInventory inventory = seatInventoryService.getOrCreateInventory(train, journeyDate, travelClass);
        // Set available seats to only 1 for test
        inventory.setAvailableSeats(1);

        BookingRequestDto req = new BookingRequestDto();
        req.setTrainId(train.getId());
        req.setTravelClass(travelClass);
        req.setJourneyDate(journeyDate);
        req.setPaymentMode(PaymentMode.UPI);

        // Request 2 seats when only 1 is available
        req.setPassengers(List.of(
                new PassengerDto("User 1", 30, "MALE", BerthPreference.LOWER),
                new PassengerDto("User 2", 30, "FEMALE", BerthPreference.UPPER)
        ));

        assertThrows(SeatNotAvailableException.class, () -> bookingService.createBooking("user@railbook.com", req));
    }
}
