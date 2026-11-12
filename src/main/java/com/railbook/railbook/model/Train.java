package com.railbook.railbook.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trains", indexes = {
    @Index(name = "idx_train_number", columnList = "trainNumber", unique = true),
    @Index(name = "idx_train_source_dest", columnList = "source_station_id, destination_station_id")
})
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Train number is required")
    @Column(nullable = false, unique = true, length = 10)
    private String trainNumber;

    @NotBlank(message = "Train name is required")
    @Column(nullable = false, length = 100)
    private String trainName;

    @Column(length = 50)
    private String trainType; // e.g. "Vande Bharat Express", "Rajdhani Express", "Shatabdi Express"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_station_id", nullable = false)
    private Station sourceStation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_station_id", nullable = false)
    private Station destinationStation;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(length = 20)
    private String durationHours; // e.g. "06h 15m"

    @Column(nullable = false, length = 50)
    private String runningDays = "MON,TUE,WED,THU,FRI,SAT,SUN"; // Comma-separated days

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TrainClassConfig> classConfigs = new ArrayList<>();

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("stopNumber ASC")
    private List<TrainSchedule> schedules = new ArrayList<>();

    public Train() {
    }

    public Train(String trainNumber, String trainName, String trainType, Station sourceStation,
                 Station destinationStation, LocalTime departureTime, LocalTime arrivalTime,
                 String durationHours, String runningDays) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.trainType = trainType;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationHours = durationHours;
        this.runningDays = runningDays != null ? runningDays : "MON,TUE,WED,THU,FRI,SAT,SUN";
        this.active = true;
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

    public Station getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(Station sourceStation) {
        this.sourceStation = sourceStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
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

    public List<TrainClassConfig> getClassConfigs() {
        return classConfigs;
    }

    public void setClassConfigs(List<TrainClassConfig> classConfigs) {
        this.classConfigs = classConfigs;
    }

    public List<TrainSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TrainSchedule> schedules) {
        this.schedules = schedules;
    }

    public boolean runsOn(DayOfWeek dayOfWeek) {
        if (runningDays == null || runningDays.isBlank()) return true;
        String dayCode = switch (dayOfWeek) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
        return runningDays.contains(dayCode);
    }
}
