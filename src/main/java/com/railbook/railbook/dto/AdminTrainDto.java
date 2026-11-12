package com.railbook.railbook.dto;

import com.railbook.railbook.model.TravelClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AdminTrainDto {

    private Long id;

    @NotBlank(message = "Train number is required")
    private String trainNumber;

    @NotBlank(message = "Train name is required")
    private String trainName;

    private String trainType = "Express";

    @NotNull(message = "Source station is required")
    private Long sourceStationId;

    @NotNull(message = "Destination station is required")
    private Long destinationStationId;

    @NotNull(message = "Departure time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime arrivalTime;

    private String durationHours;

    private String runningDays = "MON,TUE,WED,THU,FRI,SAT,SUN";

    private boolean active = true;

    private List<ClassConfigItem> classConfigs = new ArrayList<>();

    public AdminTrainDto() {
        // Initialize default class configs for all travel classes
        for (TravelClass tc : TravelClass.values()) {
            classConfigs.add(new ClassConfigItem(tc, false, 0, BigDecimal.ZERO));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public Long getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(Long sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public Long getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(Long destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(String durationHours) {
        this.durationHours = durationHours;
    }

    public String getRunningDays() {
        return runningDays;
    }

    public void setRunningDays(String runningDays) {
        this.runningDays = runningDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ClassConfigItem> getClassConfigs() {
        return classConfigs;
    }

    public void setClassConfigs(List<ClassConfigItem> classConfigs) {
        this.classConfigs = classConfigs;
    }

    public static class ClassConfigItem {
        private TravelClass travelClass;
        private boolean enabled;
        private int totalSeats;
        private BigDecimal baseFare;

        public ClassConfigItem() {
        }

        public ClassConfigItem(TravelClass travelClass, boolean enabled, int totalSeats, BigDecimal baseFare) {
            this.travelClass = travelClass;
            this.enabled = enabled;
            this.totalSeats = totalSeats;
            this.baseFare = baseFare;
        }

        public TravelClass getTravelClass() {
            return travelClass;
        }

        public void setTravelClass(TravelClass travelClass) {
            this.travelClass = travelClass;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public void setTotalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
        }

        public BigDecimal getBaseFare() {
            return baseFare;
        }

        public void setBaseFare(BigDecimal baseFare) {
            this.baseFare = baseFare;
        }
    }
}
