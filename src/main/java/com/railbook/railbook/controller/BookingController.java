package com.railbook.railbook.controller;

import com.railbook.railbook.dto.BookingRequestDto;
import com.railbook.railbook.dto.PassengerDto;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.TrainClassConfigRepository;
import com.railbook.railbook.service.BookingService;
import com.railbook.railbook.service.PdfTicketService;
import com.railbook.railbook.service.SeatInventoryService;
import com.railbook.railbook.service.TrainService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final TrainService trainService;
    private final TrainClassConfigRepository classConfigRepository;
    private final SeatInventoryService seatInventoryService;
    private final PdfTicketService pdfTicketService;

    public BookingController(BookingService bookingService,
                             TrainService trainService,
                             TrainClassConfigRepository classConfigRepository,
                             SeatInventoryService seatInventoryService,
                             PdfTicketService pdfTicketService) {
        this.bookingService = bookingService;
        this.trainService = trainService;
        this.classConfigRepository = classConfigRepository;
        this.seatInventoryService = seatInventoryService;
        this.pdfTicketService = pdfTicketService;
    }

    @GetMapping("/booking/new")
    public String showBookingPage(@RequestParam("trainId") Long trainId,
                                  @RequestParam("travelClass") String travelClassCode,
                                  @RequestParam("journeyDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate,
                                  Model model) {
        Train train = trainService.getTrainById(trainId);
        TravelClass travelClass = TravelClass.fromCode(travelClassCode);
        if (travelClass == null) {
            throw new BookingException("Invalid travel class specified");
        }

        TrainClassConfig classConfig = classConfigRepository.findByTrainAndTravelClass(train, travelClass)
                .orElseThrow(() -> new BookingException("Class not configured on this train"));

        int availableSeats = seatInventoryService.getAvailableSeats(train, journeyDate, travelClass);

        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setTrainId(trainId);
        bookingRequest.setTravelClass(travelClass);
        bookingRequest.setJourneyDate(journeyDate);
        bookingRequest.setPassengers(new ArrayList<>());
        bookingRequest.getPassengers().add(new PassengerDto());

        model.addAttribute("train", train);
        model.addAttribute("travelClass", travelClass);
        model.addAttribute("classConfig", classConfig);
        model.addAttribute("journeyDate", journeyDate);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("berthPreferences", BerthPreference.values());
        model.addAttribute("paymentModes", PaymentMode.values());

        return "booking/passenger-details";
    }

    @PostMapping("/booking/confirm")
    public String confirmBooking(@Valid @ModelAttribute("bookingRequest") BookingRequestDto bookingRequest,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Train train = trainService.getTrainById(bookingRequest.getTrainId());
            TrainClassConfig classConfig = classConfigRepository.findByTrainAndTravelClass(train, bookingRequest.getTravelClass()).orElse(null);
            int availableSeats = seatInventoryService.getAvailableSeats(train, bookingRequest.getJourneyDate(), bookingRequest.getTravelClass());

            model.addAttribute("train", train);
            model.addAttribute("travelClass", bookingRequest.getTravelClass());
            model.addAttribute("classConfig", classConfig);
            model.addAttribute("journeyDate", bookingRequest.getJourneyDate());
            model.addAttribute("availableSeats", availableSeats);
            model.addAttribute("berthPreferences", BerthPreference.values());
            model.addAttribute("paymentModes", PaymentMode.values());
            return "booking/passenger-details";
        }

        try {
            Booking booking = bookingService.createBooking(userDetails.getUsername(), bookingRequest);
            redirectAttributes.addFlashAttribute("bookingSuccess", true);
            return "redirect:/booking/confirmation/" + booking.getPnrNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("bookingError", e.getMessage());
            return "redirect:/booking/new?trainId=" + bookingRequest.getTrainId() +
                    "&travelClass=" + bookingRequest.getTravelClass().getCode() +
                    "&journeyDate=" + bookingRequest.getJourneyDate();
        }
    }

    @GetMapping("/booking/confirmation/{pnr}")
    public String showConfirmationPage(@PathVariable("pnr") String pnr, Model model) {
        Booking booking = bookingService.getBookingByPnr(pnr);
        model.addAttribute("booking", booking);
        return "booking/confirmation";
    }

    @GetMapping("/ticket/{pnr}")
    public String viewTicket(@PathVariable("pnr") String pnr, Model model) {
        Booking booking = bookingService.getBookingByPnr(pnr);
        model.addAttribute("booking", booking);
        return "booking/e-ticket";
    }

    @GetMapping("/ticket/{pnr}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable("pnr") String pnr) {
        Booking booking = bookingService.getBookingByPnr(pnr);
        byte[] pdfBytes = pdfTicketService.generateTicketPdf(booking);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"RailBook-Ticket-" + pnr + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
