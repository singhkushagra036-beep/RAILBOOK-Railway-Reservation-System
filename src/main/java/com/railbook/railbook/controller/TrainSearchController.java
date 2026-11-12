package com.railbook.railbook.controller;

import com.railbook.railbook.dto.TrainSearchDto;
import com.railbook.railbook.dto.TrainSearchResultDto;
import com.railbook.railbook.model.Station;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.service.StationService;
import com.railbook.railbook.service.TrainService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TrainSearchController {

    private final TrainService trainService;
    private final StationService stationService;

    public TrainSearchController(TrainService trainService, StationService stationService) {
        this.trainService = trainService;
        this.stationService = stationService;
    }

    @GetMapping("/search")
    public String searchTrains(@ModelAttribute("searchDto") TrainSearchDto searchDto,
                               BindingResult bindingResult,
                               Model model) {
        List<Station> stations = stationService.getAllStations();
        model.addAttribute("stations", stations);

        if (searchDto.getJourneyDate() == null) {
            searchDto.setJourneyDate(LocalDate.now());
        }

        List<TrainSearchResultDto> results = new ArrayList<>();
        if (searchDto.getSourceStationCode() != null && !searchDto.getSourceStationCode().isBlank() &&
            searchDto.getDestinationStationCode() != null && !searchDto.getDestinationStationCode().isBlank()) {
            try {
                results = trainService.searchTrains(searchDto);
            } catch (Exception e) {
                model.addAttribute("searchError", e.getMessage());
            }
        }

        model.addAttribute("searchResults", results);
        return "trains/search-results";
    }

    @GetMapping("/trains/{id}")
    public String viewTrainDetails(@PathVariable("id") Long id, Model model) {
        Train train = trainService.getTrainById(id);
        model.addAttribute("train", train);
        return "trains/train-details";
    }
}
