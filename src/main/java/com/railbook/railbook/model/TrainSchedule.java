package com.railbook.railbook.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "train_schedules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_train_stop", columnNames = {"train_id", "stopNumber"})
})
public class TrainSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private int stopNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    private int distanceKm;

    @Column(nullable = false)
    private int dayCount = 1;

    public TrainSchedule() {
    }

    public TrainSchedule(Train train, int stopNumber, Station station, LocalTime arrivalTime, LocalTime departureTime, int distanceKm, int dayCount) {
        this.train = train;
        this.stopNumber = stopNumber;
        this.station = station;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.distanceKm = distanceKm;
        this.dayCount = dayCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public int getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(int stopNumber) {
        this.stopNumber = stopNumber;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(int distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }
}
