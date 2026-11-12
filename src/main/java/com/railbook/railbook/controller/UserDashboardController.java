package com.railbook.railbook.controller;

import com.railbook.railbook.dto.PasswordChangeDto;
import com.railbook.railbook.dto.UserProfileDto;
import com.railbook.railbook.model.Booking;
import com.railbook.railbook.model.BookingStatus;
import com.railbook.railbook.model.Cancellation;
import com.railbook.railbook.service.BookingService;
import com.railbook.railbook.service.CancellationService;
import com.railbook.railbook.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class UserDashboardController {

    private final BookingService bookingService;
    private final UserService userService;
    private final CancellationService cancellationService;

    public UserDashboardController(BookingService bookingService,
                                   UserService userService,
                                   CancellationService cancellationService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.cancellationService = cancellationService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        List<Booking> bookings = bookingService.getUserBookings(email);

        LocalDate today = LocalDate.now();
        long totalBookings = bookings.size();
        long confirmedBookings = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED).count();
        long cancelledBookings = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED).count();
        long upcomingJourneys = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED && !b.getJourneyDate().isBefore(today)).count();

        model.addAttribute("user", userService.findByEmail(email));
        model.addAttribute("bookings", bookings.size() > 5 ? bookings.subList(0, 5) : bookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("upcomingJourneys", upcomingJourneys);

        return "dashboard/user-dashboard";
    }

    @GetMapping("/my-bookings")
    public String myBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        List<Booking> bookings = bookingService.getUserBookings(email);
        model.addAttribute("bookings", bookings);
        return "dashboard/my-bookings";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        model.addAttribute("profileDto", userService.getUserProfile(email));
        model.addAttribute("passwordDto", new PasswordChangeDto());
        return "dashboard/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profileDto") UserProfileDto profileDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "dashboard/profile";
        }
        try {
            userService.updateProfile(userDetails.getUsername(), profileDto);
            redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileError", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "dashboard/profile";
        }
        try {
            userService.changePassword(userDetails.getUsername(), passwordDto);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Password changed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/cancel-ticket")
    public String cancelTicket(@RequestParam("pnr") String pnr,
                               @RequestParam(value = "reason", required = false) String reason,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            Cancellation cancellation = cancellationService.cancelTicket(pnr, userDetails.getUsername(), reason);
            redirectAttributes.addFlashAttribute("cancelSuccess",
                    "Ticket with PNR " + pnr + " has been cancelled. Simulated refund amount: Rs. " +
                    cancellation.getRefundAmount() + " (Cancellation charges: Rs. " +
                    cancellation.getCancellationCharge() + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("cancelError", e.getMessage());
        }
        return "redirect:/my-bookings";
    }
}
