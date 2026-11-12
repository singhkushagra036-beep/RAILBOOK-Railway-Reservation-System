package com.railbook.railbook.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellations")
public class Cancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false)
    private LocalDateTime cancellationDate = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cancellationCharge;

    @Column(length = 255)
    private String cancellationReason;

    @Column(nullable = false, length = 50)
    private String refundStatus = "SIMULATED_PROCESSED"; // Educational simulated refund

    public Cancellation() {
    }

    public Cancellation(Booking booking, BigDecimal refundAmount, BigDecimal cancellationCharge, String cancellationReason) {
        this.booking = booking;
        this.refundAmount = refundAmount;
        this.cancellationCharge = cancellationCharge;
        this.cancellationReason = cancellationReason;
        this.cancellationDate = LocalDateTime.now();
        this.refundStatus = "SIMULATED_PROCESSED";
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

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getCancellationCharge() {
        return cancellationCharge;
    }

    public void setCancellationCharge(BigDecimal cancellationCharge) {
        this.cancellationCharge = cancellationCharge;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }
}
