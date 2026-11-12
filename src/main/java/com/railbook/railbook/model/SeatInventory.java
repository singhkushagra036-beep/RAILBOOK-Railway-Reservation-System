package com.railbook.railbook.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "seat_inventories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_train_date_class", columnNames = {"train_id", "journeyDate", "travelClass"})
}, indexes = {
    @Index(name = "idx_seat_inv_lookup", columnList = "train_id, journeyDate, travelClass")
})
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private LocalDate journeyDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TravelClass travelClass;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;

    @Column(nullable = false)
    private int bookedSeats = 0;

    @Version
    private Long version;

    public SeatInventory() {
    }

    public SeatInventory(Train train, LocalDate journeyDate, TravelClass travelClass, int totalSeats) {
        this.train = train;
        this.journeyDate = journeyDate;
        this.travelClass = travelClass;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.bookedSeats = 0;
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

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public TravelClass getTravelClass() {
        return travelClass;
    }

    public void setTravelClass(TravelClass travelClass) {
        this.travelClass = travelClass;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
