package com.railbook.railbook.repository;

import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TrainClassConfig;
import com.railbook.railbook.model.TravelClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainClassConfigRepository extends JpaRepository<TrainClassConfig, Long> {
    List<TrainClassConfig> findByTrain(Train train);
    Optional<TrainClassConfig> findByTrainAndTravelClass(Train train, TravelClass travelClass);
    void deleteByTrain(Train train);
}
