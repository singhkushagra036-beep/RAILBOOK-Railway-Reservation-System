package com.railbook.railbook.controller;

import com.railbook.railbook.dto.TrainSearchDto;
import com.railbook.railbook.model.Station;
import com.railbook.railbook.model.Train;
import com.railbook.railbook.service.StationService;
import com.railbook.railbook.service.TrainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final StationService stationService;
    private final TrainService trainService;

    public HomeController(StationService stationService, TrainService trainService) {
        this.stationService = stationService;
        this.trainService = trainService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Station> stations = stationService.getAllStations();
        List<Train> featuredTrains = trainService.getAllTrains();

        TrainSearchDto searchDto = new TrainSearchDto();
        searchDto.setJourneyDate(LocalDate.now());

        model.addAttribute("stations", stations);
        model.addAttribute("featuredTrains", featuredTrains.size() > 6 ? featuredTrains.subList(0, 6) : featuredTrains);
        model.addAttribute("searchDto", searchDto);
        return "index";
    }

    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorTitle", "Access Forbidden");
        model.addAttribute("errorMessage", "You do not have permission to access this administrative resource.");
        return "error/403";
    }

    @GetMapping("/404")
    public String notFound(Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Page Not Found");
        model.addAttribute("errorMessage", "The page or resource you are looking for does not exist.");
        return "error/404";
    }

    @GetMapping("/500")
    public String serverError(Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Server Error");
        model.addAttribute("errorMessage", "An internal error occurred. Please try again later.");
        return "error/500";
    }
}
