package com.railbook.railbook.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "passengers")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotBlank(message = "Passenger name is required")
    @Column(nullable = false, length = 100)
    private String fullName;

    @Min(value = 1, message = "Age must be at least 1")
    @Column(nullable = false)
    private int age;

    @NotBlank(message = "Gender is required")
    @Column(nullable = false, length = 20)
    private String gender; // MALE, FEMALE, OTHER

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BerthPreference berthPreference = BerthPreference.NO_PREFERENCE;

    @Column(length = 20)
    private String seatNumber; // e.g. B1-24

    @Column(length = 30)
    private String allocatedBerth; // e.g. LOWER, UPPER, WINDOW

    public Passenger() {
    }

    public Passenger(String fullName, int age, String gender, BerthPreference berthPreference) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.berthPreference = berthPreference != null ? berthPreference : BerthPreference.NO_PREFERENCE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BerthPreference getBerthPreference() {
        return berthPreference;
    }

    public void setBerthPreference(BerthPreference berthPreference) {
        this.berthPreference = berthPreference;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getAllocatedBerth() {
        return allocatedBerth;
    }

    public void setAllocatedBerth(String allocatedBerth) {
        this.allocatedBerth = allocatedBerth;
    }
}
