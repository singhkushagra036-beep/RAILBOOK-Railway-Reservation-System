package com.railbook.railbook.service;

import com.railbook.railbook.model.Booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AdminDashboardService {
    Map<String, Object> getDashboardStatistics();
    List<Booking> getRecentBookings();
}
