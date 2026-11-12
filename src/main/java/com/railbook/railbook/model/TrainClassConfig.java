package com.railbook.railbook.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "train_class_configs", uniqueConstraints = {
    @UniqueConstraint(name = "uk_train_class", columnNames = {"train_id", "travelClass"})
})
public class TrainClassConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TravelClass travelClass;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    public TrainClassConfig() {
    }

    public TrainClassConfig(Train train, TravelClass travelClass, int totalSeats, BigDecimal baseFare) {
        this.train = train;
        this.travelClass = travelClass;
        this.totalSeats = totalSeats;
        this.baseFare = baseFare;
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

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }
}
