package com.railbook.railbook.controller;

import com.railbook.railbook.dto.AdminStationDto;
import com.railbook.railbook.dto.AdminTrainDto;
import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.TrainClassConfigRepository;
import com.railbook.railbook.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;
    private final TrainService trainService;
    private final StationService stationService;
    private final BookingService bookingService;
    private final CancellationService cancellationService;
    private final UserService userService;
    private final TrainClassConfigRepository classConfigRepository;
    private final SeatInventoryService seatInventoryService;

    public AdminController(AdminDashboardService adminDashboardService,
                           TrainService trainService,
                           StationService stationService,
                           BookingService bookingService,
                           CancellationService cancellationService,
                           UserService userService,
                           TrainClassConfigRepository classConfigRepository,
                           SeatInventoryService seatInventoryService) {
        this.adminDashboardService = adminDashboardService;
        this.trainService = trainService;
        this.stationService = stationService;
        this.bookingService = bookingService;
        this.cancellationService = cancellationService;
        this.userService = userService;
        this.classConfigRepository = classConfigRepository;
        this.seatInventoryService = seatInventoryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = adminDashboardService.getDashboardStatistics();
        model.addAllAttributes(stats);
        model.addAttribute("recentBookings", adminDashboardService.getRecentBookings());
        return "admin/dashboard";
    }

    // --- Train Management ---
    @GetMapping("/trains")
    public String listTrains(Model model) {
        model.addAttribute("trains", trainService.getAllTrains());
        return "admin/trains/train-list";
    }

    @GetMapping("/trains/new")
    public String newTrainForm(Model model) {
        model.addAttribute("trainDto", new AdminTrainDto());
        model.addAttribute("stations", stationService.getAllStations());
        return "admin/trains/train-form";
    }

    @PostMapping("/trains/save")
    public String saveTrain(@Valid @ModelAttribute("trainDto") AdminTrainDto trainDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stationService.getAllStations());
            return "admin/trains/train-form";
        }
        try {
            trainService.saveTrain(trainDto);
            redirectAttributes.addFlashAttribute("successMessage", "Train saved successfully.");
            return "redirect:/admin/trains";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("stations", stationService.getAllStations());
            return "admin/trains/train-form";
        }
    }

    @GetMapping("/trains/edit/{id}")
    public String editTrainForm(@PathVariable("id") Long id, Model model) {
        Train train = trainService.getTrainById(id);
        AdminTrainDto dto = new AdminTrainDto();
        dto.setId(train.getId());
        dto.setTrainNumber(train.getTrainNumber());
        dto.setTrainName(train.getTrainName());
        dto.setTrainType(train.getTrainType());
        dto.setSourceStationId(train.getSourceStation().getId());
        dto.setDestinationStationId(train.getDestinationStation().getId());
        dto.setDepartureTime(train.getDepartureTime());
        dto.setArrivalTime(train.getArrivalTime());
        dto.setDurationHours(train.getDurationHours());
        dto.setRunningDays(train.getRunningDays());
        dto.setActive(train.isActive());

        List<TrainClassConfig> existingConfigs = classConfigRepository.findByTrain(train);
        for (AdminTrainDto.ClassConfigItem item : dto.getClassConfigs()) {
            existingConfigs.stream()
                    .filter(c -> c.getTravelClass() == item.getTravelClass())
                    .findFirst()
                    .ifPresent(c -> {
                        item.setEnabled(true);
                        item.setTotalSeats(c.getTotalSeats());
                        item.setBaseFare(c.getBaseFare());
                    });
        }

        model.addAttribute("trainDto", dto);
        model.addAttribute("stations", stationService.getAllStations());
        return "admin/trains/train-form";
    }

    @PostMapping("/trains/delete/{id}")
    public String deleteTrain(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            trainService.deleteTrain(id);
            redirectAttributes.addFlashAttribute("successMessage", "Train deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting train: " + e.getMessage());
        }
        return "redirect:/admin/trains";
    }

    // --- Station Management ---
    @GetMapping("/stations")
    public String listStations(Model model) {
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("stationDto", new AdminStationDto());
        return "admin/stations/station-list";
    }

    @PostMapping("/stations/save")
    public String saveStation(@Valid @ModelAttribute("stationDto") AdminStationDto stationDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stationService.getAllStations());
            return "admin/stations/station-list";
        }
        try {
            stationService.saveStation(stationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Station saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/stations";
    }

    @PostMapping("/stations/delete/{id}")
    public String deleteStation(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            stationService.deleteStation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Station deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete station: " + e.getMessage());
        }
        return "redirect:/admin/stations";
    }

    // --- Bookings & Cancellations Management ---
    @GetMapping("/bookings")
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings/booking-list";
    }

    @GetMapping("/bookings/{pnr}")
    public String viewBookingDetails(@PathVariable("pnr") String pnr, Model model) {
        Booking booking = bookingService.getBookingByPnr(pnr);
        model.addAttribute("booking", booking);
        return "admin/bookings/booking-details";
    }

    @GetMapping("/cancellations")
    public String listCancellations(Model model) {
        model.addAttribute("cancellations", cancellationService.getAllCancellations());
        return "admin/cancellations/cancellation-list";
    }

    @PostMapping("/bookings/{pnr}/cancel")
    public String cancelBookingByAdmin(@PathVariable("pnr") String pnr,
                                       @RequestParam(value = "reason", defaultValue = "Cancelled by Administrator") String reason,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        try {
            cancellationService.cancelTicket(pnr, userDetails.getUsername(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking " + pnr + " has been cancelled and seat inventory restored.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling booking: " + e.getMessage());
        }
        return "redirect:/admin/bookings/" + pnr;
    }

    // --- User Management ---
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/user-list";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            User updated = userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User account " + updated.getEmail() + " is now " + (updated.isEnabled() ? "Active" : "Disabled") + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-role")
    public String toggleUserRole(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            User updated = userService.toggleUserRole(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User role for " + updated.getEmail() + " changed to " + updated.getRole().name() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // --- Seat Inventory Management ---
    @GetMapping("/inventory")
    public String viewSeatInventory(@RequestParam(value = "trainId", required = false) Long trainId,
                                    @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                    Model model) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        List<Train> trains = trainService.getAllTrains();
        List<SeatInventory> inventories;

        if (trainId != null) {
            Train train = trainService.getTrainById(trainId);
            inventories = seatInventoryService.getInventoryForDate(train, queryDate);
            model.addAttribute("selectedTrain", train);
        } else {
            inventories = seatInventoryService.getInventoryByDate(queryDate);
        }

        model.addAttribute("trains", trains);
        model.addAttribute("inventories", inventories);
        model.addAttribute("selectedDate", queryDate);
        model.addAttribute("selectedTrainId", trainId);
        return "admin/inventory/inventory-list";
    }

    @PostMapping("/inventory/adjust")
    public String adjustSeatInventory(@RequestParam("inventoryId") Long inventoryId,
                                      @RequestParam("totalSeats") int totalSeats,
                                      @RequestParam("availableSeats") int availableSeats,
                                      @RequestParam(value = "trainId", required = false) Long trainId,
                                      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                      RedirectAttributes redirectAttributes) {
        try {
            seatInventoryService.adjustInventory(inventoryId, totalSeats, availableSeats);
            redirectAttributes.addFlashAttribute("successMessage", "Seat inventory adjusted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating inventory: " + e.getMessage());
        }
        String redirectUrl = "redirect:/admin/inventory?date=" + (date != null ? date : LocalDate.now());
        if (trainId != null) {
            redirectUrl += "&trainId=" + trainId;
        }
        return redirectUrl;
    }

    // --- Train Route Schedules & Halts Management ---
    @GetMapping("/trains/{id}/schedules")
    public String manageTrainSchedules(@PathVariable("id") Long trainId, Model model) {
        Train train = trainService.getTrainById(trainId);
        List<TrainSchedule> schedules = trainService.getTrainSchedules(trainId);
        List<Station> stations = stationService.getAllStations();

        model.addAttribute("train", train);
        model.addAttribute("schedules", schedules);
        model.addAttribute("stations", stations);
        return "admin/trains/train-schedules";
    }

    @PostMapping("/trains/{id}/schedules/add")
    public String addTrainSchedule(@PathVariable("id") Long trainId,
                                   @RequestParam("stopNumber") int stopNumber,
                                   @RequestParam("stationId") Long stationId,
                                   @RequestParam(value = "arrivalTime", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime arrivalTime,
                                   @RequestParam(value = "departureTime", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime departureTime,
                                   @RequestParam(value = "distanceKm", defaultValue = "0") int distanceKm,
                                   @RequestParam(value = "dayCount", defaultValue = "1") int dayCount,
                                   RedirectAttributes redirectAttributes) {
        try {
            trainService.addTrainSchedule(trainId, stopNumber, stationId, arrivalTime, departureTime, distanceKm, dayCount);
            redirectAttributes.addFlashAttribute("successMessage", "Route stop added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding stop: " + e.getMessage());
        }
        return "redirect:/admin/trains/" + trainId + "/schedules";
    }

    @PostMapping("/trains/{id}/schedules/delete/{scheduleId}")
    public String deleteTrainSchedule(@PathVariable("id") Long trainId,
                                      @PathVariable("scheduleId") Long scheduleId,
                                      RedirectAttributes redirectAttributes) {
        try {
            trainService.deleteTrainSchedule(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "Route stop removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting stop: " + e.getMessage());
        }
        return "redirect:/admin/trains/" + trainId + "/schedules";
    }
}
