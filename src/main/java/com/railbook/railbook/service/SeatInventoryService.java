package com.railbook.railbook.service;

import com.railbook.railbook.model.SeatInventory;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TravelClass;
import java.time.LocalDate;
import java.util.List;

public interface SeatInventoryService {
    SeatInventory getOrCreateInventory(Train train, LocalDate journeyDate, TravelClass travelClass);
    int getAvailableSeats(Train train, LocalDate journeyDate, TravelClass travelClass);
    SeatInventory reserveSeatsWithLock(Train train, LocalDate journeyDate, TravelClass travelClass, int requestedSeats);
    void restoreSeats(Train train, LocalDate journeyDate, TravelClass travelClass, int seatsToRestore);
    List<SeatInventory> getInventoryForDate(Train train, LocalDate journeyDate);
}
