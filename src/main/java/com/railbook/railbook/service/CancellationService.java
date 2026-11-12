package com.railbook.railbook.service;

import com.railbook.railbook.model.Booking;
import com.railbook.railbook.model.Cancellation;

import java.math.BigDecimal;
import java.util.List;

public interface CancellationService {
    Cancellation cancelTicket(String pnr, String userEmail, String cancellationReason);
    List<Cancellation> getAllCancellations();
    BigDecimal calculateSimulatedRefund(Booking booking);
    BigDecimal calculateCancellationCharges(Booking booking);
}
