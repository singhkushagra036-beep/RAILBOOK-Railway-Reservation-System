package com.railbook.railbook.repository;

import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    List<TrainSchedule> findByTrainOrderByStopNumberAsc(Train train);
    void deleteByTrain(Train train);
}
