package com.railbook.railbook.repository;

import com.railbook.railbook.model.Station;
import com.railbook.railbook.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByTrainNumber(String trainNumber);
    boolean existsByTrainNumber(String trainNumber);
    List<Train> findBySourceStationAndDestinationStationAndActiveTrue(Station sourceStation, Station destinationStation);
    List<Train> findAllByActiveTrue();

    @Query("SELECT t FROM Train t WHERE t.active = true AND " +
           "((t.sourceStation = :source AND t.destinationStation = :dest) OR " +
           "(EXISTS (SELECT s1 FROM TrainSchedule s1 WHERE s1.train = t AND s1.station = :source) AND " +
           " EXISTS (SELECT s2 FROM TrainSchedule s2 WHERE s2.train = t AND s2.station = :dest AND s2.stopNumber > " +
           "   (SELECT s3.stopNumber FROM TrainSchedule s3 WHERE s3.train = t AND s3.station = :source))))")
    List<Train> findTrainsBetweenStations(@Param("source") Station source, @Param("dest") Station dest);
}
