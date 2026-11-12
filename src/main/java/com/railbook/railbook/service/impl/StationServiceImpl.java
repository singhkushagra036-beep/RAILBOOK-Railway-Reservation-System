package com.railbook.railbook.service.impl;

import com.railbook.railbook.dto.AdminStationDto;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.model.Station;
import com.railbook.railbook.repository.StationRepository;
import com.railbook.railbook.service.StationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;

    public StationServiceImpl(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Station> getAllStations() {
        return stationRepository.findAllByOrderByStationNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Station getStationById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Station getStationByCode(String code) {
        if (code == null) return null;
        return stationRepository.findByStationCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with code: " + code));
    }

    @Override
    public Station saveStation(AdminStationDto dto) {
        String code = dto.getStationCode().trim().toUpperCase();
        Station station;

        if (dto.getId() != null) {
            station = getStationById(dto.getId());
            if (!station.getStationCode().equalsIgnoreCase(code) && stationRepository.existsByStationCode(code)) {
                throw new BookingException("Station code " + code + " already exists");
            }
        } else {
            if (stationRepository.existsByStationCode(code)) {
                throw new BookingException("Station code " + code + " already exists");
            }
            station = new Station();
        }

        station.setStationCode(code);
        station.setStationName(dto.getStationName().trim());
        station.setCity(dto.getCity().trim());
        station.setState(dto.getState() != null ? dto.getState().trim() : null);

        return stationRepository.save(station);
    }

    @Override
    public void deleteStation(Long id) {
        Station station = getStationById(id);
        stationRepository.delete(station);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalStationCount() {
        return stationRepository.count();
    }
}
