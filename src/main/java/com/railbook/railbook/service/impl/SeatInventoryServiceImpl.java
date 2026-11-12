package com.railbook.railbook.service.impl;

import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.exception.SeatNotAvailableException;
import com.railbook.railbook.model.SeatInventory;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TrainClassConfig;
import com.railbook.railbook.model.TravelClass;
import com.railbook.railbook.repository.SeatInventoryRepository;
import com.railbook.railbook.repository.TrainClassConfigRepository;
import com.railbook.railbook.service.SeatInventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeatInventoryServiceImpl implements SeatInventoryService {

    private final SeatInventoryRepository seatInventoryRepository;
    private final TrainClassConfigRepository classConfigRepository;

    public SeatInventoryServiceImpl(SeatInventoryRepository seatInventoryRepository,
                                    TrainClassConfigRepository classConfigRepository) {
        this.seatInventoryRepository = seatInventoryRepository;
        this.classConfigRepository = classConfigRepository;
    }

    @Override
    public SeatInventory getOrCreateInventory(Train train, LocalDate journeyDate, TravelClass travelClass) {
        Optional<SeatInventory> existing = seatInventoryRepository.findByTrainAndJourneyDateAndTravelClass(train, journeyDate, travelClass);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Look up total seats configured for this train and class
        TrainClassConfig config = classConfigRepository.findByTrainAndTravelClass(train, travelClass)
                .orElseThrow(() -> new ResourceNotFoundException("Class " + travelClass.getCode() + " not configured for train " + train.getTrainNumber()));

        SeatInventory inventory = new SeatInventory(train, journeyDate, travelClass, config.getTotalSeats());
        return seatInventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableSeats(Train train, LocalDate journeyDate, TravelClass travelClass) {
        Optional<SeatInventory> invOpt = seatInventoryRepository.findByTrainAndJourneyDateAndTravelClass(train, journeyDate, travelClass);
        if (invOpt.isPresent()) {
            return invOpt.get().getAvailableSeats();
        }

        Optional<TrainClassConfig> configOpt = classConfigRepository.findByTrainAndTravelClass(train, travelClass);
        return configOpt.map(TrainClassConfig::getTotalSeats).orElse(0);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SeatInventory reserveSeatsWithLock(Train train, LocalDate journeyDate, TravelClass travelClass, int requestedSeats) {
        // Fetch directly with PESSIMISTIC_WRITE lock to prevent race conditions & double-booking
        Optional<SeatInventory> invOpt = seatInventoryRepository.findWithLockByTrainAndJourneyDateAndTravelClass(train, journeyDate, travelClass);
        SeatInventory inventory;

        if (invOpt.isPresent()) {
            inventory = invOpt.get();
        } else {
            TrainClassConfig config = classConfigRepository.findByTrainAndTravelClass(train, travelClass)
                    .orElseThrow(() -> new ResourceNotFoundException("Class " + travelClass.getCode() + " not configured for train " + train.getTrainNumber()));
            inventory = new SeatInventory(train, journeyDate, travelClass, config.getTotalSeats());
            inventory = seatInventoryRepository.saveAndFlush(inventory);
            inventory = seatInventoryRepository.findWithLockByTrainAndJourneyDateAndTravelClass(train, journeyDate, travelClass)
                    .orElse(inventory);
        }

        if (inventory.getAvailableSeats() < requestedSeats) {
            throw new SeatNotAvailableException(
                    "Only " + inventory.getAvailableSeats() + " seat(s) available in " +
                    travelClass.getCode() + " for date " + journeyDate + ". Requested: " + requestedSeats
            );
        }

        inventory.setAvailableSeats(inventory.getAvailableSeats() - requestedSeats);
        inventory.setBookedSeats(inventory.getBookedSeats() + requestedSeats);

        return seatInventoryRepository.saveAndFlush(inventory);
    }

    @Override
    public void restoreSeats(Train train, LocalDate journeyDate, TravelClass travelClass, int seatsToRestore) {
        Optional<SeatInventory> invOpt = seatInventoryRepository.findByTrainAndJourneyDateAndTravelClass(train, journeyDate, travelClass);
        if (invOpt.isPresent()) {
            SeatInventory inventory = invOpt.get();
            int newAvailable = Math.min(inventory.getTotalSeats(), inventory.getAvailableSeats() + seatsToRestore);
            int newBooked = Math.max(0, inventory.getBookedSeats() - seatsToRestore);
            inventory.setAvailableSeats(newAvailable);
            inventory.setBookedSeats(newBooked);
            seatInventoryRepository.save(inventory);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatInventory> getInventoryForDate(Train train, LocalDate journeyDate) {
        return seatInventoryRepository.findByTrainAndJourneyDate(train, journeyDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatInventory> getInventoryByDate(LocalDate date) {
        return seatInventoryRepository.findByJourneyDateOrderByTrainTrainNumberAsc(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatInventory> getInventoryByTrain(Train train) {
        return seatInventoryRepository.findByTrainOrderByJourneyDateAscTravelClassAsc(train);
    }

    @Override
    public SeatInventory adjustInventory(Long inventoryId, int newTotalSeats, int newAvailableSeats) {
        SeatInventory inventory = seatInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat inventory record not found with id: " + inventoryId));

        if (newTotalSeats < 0 || newAvailableSeats < 0) {
            throw new BookingException("Seat capacities cannot be negative");
        }
        if (newAvailableSeats > newTotalSeats) {
            throw new BookingException("Available seats cannot exceed total seats");
        }

        inventory.setTotalSeats(newTotalSeats);
        inventory.setAvailableSeats(newAvailableSeats);
        inventory.setBookedSeats(newTotalSeats - newAvailableSeats);
        return seatInventoryRepository.save(inventory);
    }
}
