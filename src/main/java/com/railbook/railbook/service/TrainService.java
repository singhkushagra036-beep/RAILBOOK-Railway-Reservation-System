package com.railbook.railbook.service;

import com.railbook.railbook.dto.AdminTrainDto;
import com.railbook.railbook.dto.TrainSearchDto;
import com.railbook.railbook.dto.TrainSearchResultDto;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.model.TrainSchedule;

import java.time.LocalTime;
import java.util.List;

public interface TrainService {
    List<TrainSearchResultDto> searchTrains(TrainSearchDto searchDto);
    List<Train> getAllTrains();
    Train getTrainById(Long id);
    Train getTrainByNumber(String trainNumber);
    Train saveTrain(AdminTrainDto dto);
    void deleteTrain(Long id);
    long getTotalTrainCount();
    List<TrainSchedule> getTrainSchedules(Long trainId);
    TrainSchedule addTrainSchedule(Long trainId, int stopNumber, Long stationId, LocalTime arrivalTime, LocalTime departureTime, int distanceKm, int dayCount);
    void deleteTrainSchedule(Long scheduleId);
}
