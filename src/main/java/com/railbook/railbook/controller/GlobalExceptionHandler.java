package com.railbook.railbook.controller;

import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.exception.SeatNotAvailableException;
import com.railbook.railbook.exception.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler({SeatNotAvailableException.class, BookingException.class, UserAlreadyExistsException.class})
    public String handleBusinessException(Exception ex, Model model) {
        log.warn("Business validation error: {}", ex.getMessage());
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorTitle", "Booking / Validation Notice");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/general-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unhandled application exception", ex);
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request. Please try again.");
        return "error/500";
    }
}
