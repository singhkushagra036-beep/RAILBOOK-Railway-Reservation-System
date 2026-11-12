package com.railbook.railbook.dto;

import com.railbook.railbook.model.PaymentMode;
import com.railbook.railbook.model.TravelClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingRequestDto {

    @NotNull(message = "Train selection is required")
    private Long trainId;

    @NotNull(message = "Travel class is required")
    private TravelClass travelClass;

    @NotNull(message = "Journey date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate journeyDate;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerDto> passengers = new ArrayList<>();

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode = PaymentMode.UPI;

    public BookingRequestDto() {
        this.passengers.add(new PassengerDto());
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public TravelClass getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(TravelClass travelClass) {
        this.travelClass = travelClass;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public List<PassengerDto> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerDto> passengers) {
        this.passengers = passengers;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }
}
