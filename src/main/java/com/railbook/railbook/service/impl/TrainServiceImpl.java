package com.railbook.railbook.service.impl;

import com.railbook.railbook.dto.AdminTrainDto;
import com.railbook.railbook.dto.TrainSearchDto;
import com.railbook.railbook.dto.TrainSearchResultDto;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.StationRepository;
import com.railbook.railbook.repository.TrainClassConfigRepository;
import com.railbook.railbook.repository.TrainRepository;
import com.railbook.railbook.repository.TrainScheduleRepository;
import com.railbook.railbook.service.SeatInventoryService;
import com.railbook.railbook.service.TrainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TrainClassConfigRepository classConfigRepository;
    private final SeatInventoryService seatInventoryService;
    private final TrainScheduleRepository trainScheduleRepository;

    public TrainServiceImpl(TrainRepository trainRepository,
                            StationRepository stationRepository,
                            TrainClassConfigRepository classConfigRepository,
                            SeatInventoryService seatInventoryService,
                            TrainScheduleRepository trainScheduleRepository) {
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.classConfigRepository = classConfigRepository;
        this.seatInventoryService = seatInventoryService;
        this.trainScheduleRepository = trainScheduleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainSearchResultDto> searchTrains(TrainSearchDto searchDto) {
        Station source = stationRepository.findByStationCode(searchDto.getSourceStationCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Source station not found: " + searchDto.getSourceStationCode()));

        Station destination = stationRepository.findByStationCode(searchDto.getDestinationStationCode().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found: " + searchDto.getDestinationStationCode()));

        if (source.getId().equals(destination.getId())) {
            throw new BookingException("Source and Destination stations cannot be the same");
        }

        LocalDate journeyDate = searchDto.getJourneyDate() != null ? searchDto.getJourneyDate() : LocalDate.now();

        List<Train> matchingTrains = trainRepository.findTrainsBetweenStations(source, destination);
        List<TrainSearchResultDto> results = new ArrayList<>();

        for (Train train : matchingTrains) {
            // Check running days
            if (!train.runsOn(journeyDate.getDayOfWeek())) {
                continue;
            }

            TrainSearchResultDto resultDto = new TrainSearchResultDto();
            resultDto.setTrainId(train.getId());
            resultDto.setTrainNumber(train.getTrainNumber());
            resultDto.setTrainName(train.getTrainName());
            resultDto.setTrainType(train.getTrainType());
            resultDto.setSourceStationName(train.getSourceStation().getStationName());
            resultDto.setSourceStationCode(train.getSourceStation().getStationCode());
            resultDto.setDestinationStationName(train.getDestinationStation().getStationName());
            resultDto.setDestinationStationCode(train.getDestinationStation().getStationCode());
            resultDto.setDepartureTime(train.getDepartureTime());
            resultDto.setArrivalTime(train.getArrivalTime());
            resultDto.setDurationHours(train.getDurationHours());
            resultDto.setRunningDays(train.getRunningDays());
            resultDto.setJourneyDate(journeyDate);

            List<TrainClassConfig> classConfigs = classConfigRepository.findByTrain(train);
            List<TrainSearchResultDto.ClassAvailabilityDto> classAvailabilities = new ArrayList<>();

            for (TrainClassConfig config : classConfigs) {
                TravelClass tc = config.getTravelClass();
                int availableSeats = seatInventoryService.getAvailableSeats(train, journeyDate, tc);

                TrainSearchResultDto.ClassAvailabilityDto availDto = new TrainSearchResultDto.ClassAvailabilityDto(
                        tc,
                        availableSeats,
                        config.getTotalSeats(),
                        config.getBaseFare()
                );
                classAvailabilities.add(availDto);
            }

            resultDto.setClassAvailabilities(classAvailabilities);
            results.add(resultDto);
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Train getTrainById(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Train getTrainByNumber(String trainNumber) {
        return trainRepository.findByTrainNumber(trainNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with number: " + trainNumber));
    }

    @Override
    public Train saveTrain(AdminTrainDto dto) {
        Train train;
        String number = dto.getTrainNumber().trim();

        if (dto.getId() != null) {
            train = getTrainById(dto.getId());
            if (!train.getTrainNumber().equals(number) && trainRepository.existsByTrainNumber(number)) {
                throw new BookingException("Train number " + number + " already exists");
            }
        } else {
            if (trainRepository.existsByTrainNumber(number)) {
                throw new BookingException("Train number " + number + " already exists");
            }
            train = new Train();
        }

        Station source = stationRepository.findById(dto.getSourceStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Source station not found"));
        Station destination = stationRepository.findById(dto.getDestinationStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found"));

        if (source.getId().equals(destination.getId())) {
            throw new BookingException("Source and Destination stations cannot be identical");
        }

        train.setTrainNumber(number);
        train.setTrainName(dto.getTrainName().trim());
        train.setTrainType(dto.getTrainType() != null ? dto.getTrainType().trim() : "Express");
        train.setSourceStation(source);
        train.setDestinationStation(destination);
        train.setDepartureTime(dto.getDepartureTime());
        train.setArrivalTime(dto.getArrivalTime());
        train.setDurationHours(dto.getDurationHours());
        train.setRunningDays(dto.getRunningDays() != null ? dto.getRunningDays().trim() : "MON,TUE,WED,THU,FRI,SAT,SUN");
        train.setActive(dto.isActive());

        Train savedTrain = trainRepository.save(train);

        // Update class configs
        if (dto.getClassConfigs() != null && !dto.getClassConfigs().isEmpty()) {
            classConfigRepository.deleteByTrain(savedTrain);
            for (AdminTrainDto.ClassConfigItem item : dto.getClassConfigs()) {
                if (item.isEnabled() && item.getTotalSeats() > 0 && item.getBaseFare() != null && item.getBaseFare().compareTo(BigDecimal.ZERO) > 0) {
                    TrainClassConfig cfg = new TrainClassConfig(savedTrain, item.getTravelClass(), item.getTotalSeats(), item.getBaseFare());
                    classConfigRepository.save(cfg);
                }
            }
        }

        return savedTrain;
    }

    @Override
    public void deleteTrain(Long id) {
        Train train = getTrainById(id);
        trainRepository.delete(train);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalTrainCount() {
        return trainRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainSchedule> getTrainSchedules(Long trainId) {
        Train train = getTrainById(trainId);
        return trainScheduleRepository.findByTrainOrderByStopNumberAsc(train);
    }

    @Override
    public TrainSchedule addTrainSchedule(Long trainId, int stopNumber, Long stationId, LocalTime arrivalTime, LocalTime departureTime, int distanceKm, int dayCount) {
        Train train = getTrainById(trainId);
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + stationId));

        TrainSchedule schedule = new TrainSchedule(train, stopNumber, station, arrivalTime, departureTime, distanceKm, dayCount > 0 ? dayCount : 1);
        return trainScheduleRepository.save(schedule);
    }

    @Override
    public void deleteTrainSchedule(Long scheduleId) {
        TrainSchedule schedule = trainScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule halt not found with id: " + scheduleId));
        trainScheduleRepository.delete(schedule);
    }
}
