package com.railbook.railbook.service;

import com.railbook.railbook.dto.AdminStationDto;
import com.railbook.railbook.model.Station;
import java.util.List;

public interface StationService {
    List<Station> getAllStations();
    Station getStationById(Long id);
    Station getStationByCode(String code);
    Station saveStation(AdminStationDto dto);
    void deleteStation(Long id);
    long getTotalStationCount();
}
