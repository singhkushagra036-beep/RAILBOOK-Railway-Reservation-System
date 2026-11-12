package com.railbook.railbook.service.impl;

import com.railbook.railbook.model.Booking;
import com.railbook.railbook.model.BookingStatus;
import com.railbook.railbook.repository.BookingRepository;
import com.railbook.railbook.repository.StationRepository;
import com.railbook.railbook.repository.TrainRepository;
import com.railbook.railbook.repository.UserRepository;
import com.railbook.railbook.service.AdminDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardServiceImpl(UserRepository userRepository,
                                     TrainRepository trainRepository,
                                     StationRepository stationRepository,
                                     BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalTrains = trainRepository.count();
        long totalStations = stationRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED);
        long cancelledBookings = bookingRepository.countByBookingStatus(BookingStatus.CANCELLED);
        BigDecimal totalRevenue = bookingRepository.sumTotalConfirmedRevenue();

        stats.put("totalUsers", totalUsers);
        stats.put("totalTrains", totalTrains);
        stats.put("totalStations", totalStations);
        stats.put("totalBookings", totalBookings);
        stats.put("confirmedBookings", confirmedBookings);
        stats.put("cancelledBookings", cancelledBookings);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public List<Booking> getRecentBookings() {
        List<Booking> all = bookingRepository.findAllByOrderByBookingDateDesc();
        return all.size() > 10 ? all.subList(0, 10) : all;
    }
}
