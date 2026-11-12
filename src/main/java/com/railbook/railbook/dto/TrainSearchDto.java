package com.railbook.railbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class TrainSearchDto {

    @NotBlank(message = "Source station is required")
    private String sourceStationCode;

    @NotBlank(message = "Destination station is required")
    private String destinationStationCode;

    @NotNull(message = "Journey date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate journeyDate;

    private String travelClassCode;

    public TrainSearchDto() {
        this.journeyDate = LocalDate.now();
    }

    public TrainSearchDto(String sourceStationCode, String destinationStationCode, LocalDate journeyDate, String travelClassCode) {
        this.sourceStationCode = sourceStationCode;
        this.destinationStationCode = destinationStationCode;
        this.journeyDate = journeyDate != null ? journeyDate : LocalDate.now();
        this.travelClassCode = travelClassCode;
    }

    public String getSourceStationCode() {
        return sourceStationCode;
    }

    public void setSourceStationCode(String sourceStationCode) {
        this.sourceStationCode = sourceStationCode;
    }

    public String getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(String destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getTravelClassCode() {
        return travelClassCode;
    }

    public void setTravelClassCode(String travelClassCode) {
        this.travelClassCode = travelClassCode;
    }
}
