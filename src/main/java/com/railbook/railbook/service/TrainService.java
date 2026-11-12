package com.railbook.railbook.service;

import com.railbook.railbook.dto.AdminTrainDto;
import com.railbook.railbook.dto.TrainSearchDto;
import com.railbook.railbook.dto.TrainSearchResultDto;
import com.railbook.railbook.model.Train;

import java.util.List;

public interface TrainService {
    List<TrainSearchResultDto> searchTrains(TrainSearchDto searchDto);
    List<Train> getAllTrains();
    Train getTrainById(Long id);
    Train getTrainByNumber(String trainNumber);
    Train saveTrain(AdminTrainDto dto);
    void deleteTrain(Long id);
    long getTotalTrainCount();
}
