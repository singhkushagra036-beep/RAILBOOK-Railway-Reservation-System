package com.railbook.railbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdminStationDto {

    private Long id;

    @NotBlank(message = "Station code is required")
    @Size(min = 2, max = 10, message = "Station code must be between 2 and 10 characters")
    private String stationCode;

    @NotBlank(message = "Station name is required")
    private String stationName;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    public AdminStationDto() {
    }

    public AdminStationDto(Long id, String stationCode, String stationName, String city, String state) {
        this.id = id;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
