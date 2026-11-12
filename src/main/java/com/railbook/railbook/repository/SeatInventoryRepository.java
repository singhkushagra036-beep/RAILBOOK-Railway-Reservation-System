package com.railbook.railbook.repository;

import com.railbook.railbook.model.SeatInventory;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TravelClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    Optional<SeatInventory> findByTrainAndJourneyDateAndTravelClass(Train train, LocalDate journeyDate, TravelClass travelClass);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatInventory s WHERE s.train = :train AND s.journeyDate = :journeyDate AND s.travelClass = :travelClass")
    Optional<SeatInventory> findWithLockByTrainAndJourneyDateAndTravelClass(
            @Param("train") Train train,
            @Param("journeyDate") LocalDate journeyDate,
            @Param("travelClass") TravelClass travelClass
    );

    List<SeatInventory> findByTrainAndJourneyDate(Train train, LocalDate journeyDate);
}
