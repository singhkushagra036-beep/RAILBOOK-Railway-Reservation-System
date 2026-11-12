package com.railbook.railbook;

import com.railbook.railbook.dto.BookingRequestDto;
import com.railbook.railbook.dto.PassengerDto;
import com.railbook.railbook.model.BerthPreference;
import com.railbook.railbook.model.PaymentMode;
import com.railbook.railbook.model.SeatInventory;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TravelClass;
import com.railbook.railbook.repository.SeatInventoryRepository;
import com.railbook.railbook.repository.TrainRepository;
import com.railbook.railbook.service.BookingService;
import com.railbook.railbook.service.SeatInventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingConcurrencyIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SeatInventoryService seatInventoryService;

    @Autowired
    private SeatInventoryRepository seatInventoryRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Test
    @DisplayName("Concurrency: Simultaneous booking requests must never double-book or exceed available seats")
    void testConcurrentBookingIntegrity() throws InterruptedException {
        Train train = trainRepository.findByTrainNumber("12004")
                .orElseThrow(() -> new IllegalStateException("Train 12004 not found"));

        LocalDate testDate = LocalDate.now().plusDays(20);
        TravelClass travelClass = TravelClass.EXEC_CHAIR_CAR;

        // Ensure fresh inventory with exactly 5 available seats
        SeatInventory inventory = seatInventoryService.getOrCreateInventory(train, testDate, travelClass);
        inventory.setTotalSeats(5);
        inventory.setAvailableSeats(5);
        inventory.setBookedSeats(0);
        seatInventoryRepository.saveAndFlush(inventory);

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Wait for all threads to align and fire simultaneously
                    BookingRequestDto req = new BookingRequestDto();
                    req.setTrainId(train.getId());
                    req.setTravelClass(travelClass);
                    req.setJourneyDate(testDate);
                    req.setPaymentMode(PaymentMode.UPI);
                    req.setPassengers(List.of(new PassengerDto("Passenger " + index, 25 + index, "MALE", BerthPreference.NO_PREFERENCE)));

                    bookingService.createBooking("user@railbook.com", req);
                    successfulBookings.incrementAndGet();
                } catch (Exception e) {
                    failedBookings.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Fire all 10 threads concurrently!
        doneLatch.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        // Exactly 5 should succeed (as initial capacity was 5), and 5 should fail due to pessimistic locking
        assertEquals(5, successfulBookings.get(), "Exactly 5 bookings should succeed for 5 seats");
        assertEquals(5, failedBookings.get(), "Remaining 5 concurrent attempts must fail without double-booking");

        // Verify remaining available seats is exactly 0
        SeatInventory finalInventory = seatInventoryRepository.findByTrainAndJourneyDateAndTravelClass(train, testDate, travelClass).orElseThrow();
        assertEquals(0, finalInventory.getAvailableSeats(), "Available seats must be exactly 0");
        assertEquals(5, finalInventory.getBookedSeats(), "Booked seats must be exactly 5");
    }
}