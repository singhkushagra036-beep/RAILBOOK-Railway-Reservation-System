package com.railbook.railbook.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "stations", indexes = {
    @Index(name = "idx_station_code", columnList = "stationCode", unique = true)
})
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Station code is required")
    @Column(nullable = false, unique = true, length = 10)
    private String stationCode;

    @NotBlank(message = "Station name is required")
    @Column(nullable = false, length = 100)
    private String stationName;

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 60)
    private String city;

    @Column(length = 60)
    private String state;

    public Station() {
    }

    public Station(String stationCode, String stationName, String city, String state) {
        this.stationCode = stationCode != null ? stationCode.trim().toUpperCase() : null;
        this.stationName = stationName != null ? stationName.trim() : null;
        this.city = city != null ? city.trim() : null;
        this.state = state != null ? state.trim() : null;
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
        this.stationCode = stationCode != null ? stationCode.trim().toUpperCase() : null;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName != null ? stationName.trim() : null;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city != null ? city.trim() : null;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state != null ? state.trim() : null;
    }

    public String getFormattedDisplay() {
        return stationName + " (" + stationCode + ")";
    }
}
