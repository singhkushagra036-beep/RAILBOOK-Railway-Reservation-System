package com.railbook.railbook.controller;

import com.railbook.railbook.model.Booking;
import com.railbook.railbook.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PnrController {

    private final BookingService bookingService;

    public PnrController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/pnr")
    public String pnrPage() {
        return "pnr/pnr-search";
    }

    @GetMapping("/pnr-status")
    public String checkPnrStatus(@RequestParam(value = "pnr", required = false) String pnr, Model model) {
        if (pnr == null || pnr.trim().isBlank()) {
            model.addAttribute("errorMessage", "Please enter a valid 10-digit PNR number.");
            return "pnr/pnr-search";
        }

        try {
            Booking booking = bookingService.getBookingByPnr(pnr.trim());
            model.addAttribute("booking", booking);
            model.addAttribute("searchedPnr", pnr.trim());
            return "pnr/pnr-status";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "No active booking found for PNR: " + pnr.trim() + ". Please verify and try again.");
            model.addAttribute("searchedPnr", pnr.trim());
            return "pnr/pnr-search";
        }
    }
}
