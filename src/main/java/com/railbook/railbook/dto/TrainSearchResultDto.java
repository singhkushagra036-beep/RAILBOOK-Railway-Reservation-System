package com.railbook.railbook.dto;

import com.railbook.railbook.model.TravelClass;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TrainSearchResultDto {

    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String trainType;
    private String sourceStationName;
    private String sourceStationCode;
    private String destinationStationName;
    private String destinationStationCode;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String durationHours;
    private String runningDays;
    private LocalDate journeyDate;
    private List<ClassAvailabilityDto> classAvailabilities = new ArrayList<>();

    public TrainSearchResultDto() {
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
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

    public String getSourceStationName() {
        return sourceStationName;
    }

    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }

    public String getSourceStationCode() {
        return sourceStationCode;
    }

    public void setSourceStationCode(String sourceStationCode) {
        this.sourceStationCode = sourceStationCode;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }

    public String getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(String destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
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

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public List<ClassAvailabilityDto> getClassAvailabilities() {
        return classAvailabilities;
    }

    public void setClassAvailabilities(List<ClassAvailabilityDto> classAvailabilities) {
        this.classAvailabilities = classAvailabilities;
    }

    public static class ClassAvailabilityDto {
        private TravelClass travelClass;
        private String classCode;
        private String className;
        private int availableSeats;
        private int totalSeats;
        private BigDecimal fare;
        private boolean isAvailable;

        public ClassAvailabilityDto() {
        }

        public ClassAvailabilityDto(TravelClass travelClass, int availableSeats, int totalSeats, BigDecimal fare) {
            this.travelClass = travelClass;
            this.classCode = travelClass.getCode();
            this.className = travelClass.getDisplayName();
            this.availableSeats = availableSeats;
            this.totalSeats = totalSeats;
            this.fare = fare;
            this.isAvailable = availableSeats > 0;
        }

        public TravelClass getTravelClass() {
            return travelClass;
        }

        public void setTravelClass(TravelClass travelClass) {
            this.travelClass = travelClass;
        }

        public String getClassCode() {
            return classCode;
        }

        public void setClassCode(String classCode) {
            this.classCode = classCode;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getAvailableSeats() {
            return availableSeats;
        }

        public void setAvailableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
            this.isAvailable = availableSeats > 0;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public void setTotalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
        }

        public BigDecimal getFare() {
            return fare;
        }

        public void setFare(BigDecimal fare) {
            this.fare = fare;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }
    }
}
