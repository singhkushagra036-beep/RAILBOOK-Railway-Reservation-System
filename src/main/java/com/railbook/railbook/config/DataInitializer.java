package com.railbook.railbook.config;

import com.railbook.railbook.model.*;
import com.railbook.railbook.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final TrainClassConfigRepository classConfigRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${railbook.demo-data.enabled:true}")
    private boolean demoDataEnabled;

    public DataInitializer(UserRepository userRepository,
                           StationRepository stationRepository,
                           TrainRepository trainRepository,
                           TrainClassConfigRepository classConfigRepository,
                           SeatInventoryRepository seatInventoryRepository,
                           TrainScheduleRepository trainScheduleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.classConfigRepository = classConfigRepository;
        this.seatInventoryRepository = seatInventoryRepository;
        this.trainScheduleRepository = trainScheduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoDataEnabled) {
            log.info("Demo data initialization is disabled via configuration.");
            return;
        }

        initUsers();
        Map<String, Station> stationMap = initStations();
        initTrains(stationMap);
        initSchedules(stationMap);
        log.info("RailBook demonstration data initialization complete.");
    }

    private void initUsers() {
        if (!userRepository.existsByEmail("admin@railbook.com")) {
            User admin = new User(
                    "RailBook System Admin",
                    "admin@railbook.com",
                    passwordEncoder.encode("admin123"),
                    "9876543210",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            log.info("Created Demo Admin account: admin@railbook.com / admin123");
        }

        if (!userRepository.existsByEmail("user@railbook.com")) {
            User demoUser = new User(
                    "Rahul Sharma",
                    "user@railbook.com",
                    passwordEncoder.encode("user123"),
                    "9876500001",
                    Role.ROLE_USER
            );
            userRepository.save(demoUser);
            log.info("Created Demo User account: user@railbook.com / user123");
        }
    }

    private Map<String, Station> initStations() {
        Map<String, Station> stationMap = new HashMap<>();

        String[][] stationData = {
                {"NDLS", "New Delhi", "New Delhi", "Delhi"},
                {"MMCT", "Mumbai Central", "Mumbai", "Maharashtra"},
                {"HWH", "Howrah Junction", "Kolkata", "West Bengal"},
                {"SBC", "KSR Bengaluru", "Bengaluru", "Karnataka"},
                {"MAS", "MGR Chennai Central", "Chennai", "Tamil Nadu"},
                {"BSB", "Varanasi Junction", "Varanasi", "Uttar Pradesh"},
                {"LKO", "Lucknow Charbagh", "Lucknow", "Uttar Pradesh"},
                {"PNBE", "Patna Junction", "Patna", "Bihar"},
                {"JP", "Jaipur Junction", "Jaipur", "Rajasthan"},
                {"ADI", "Ahmedabad Junction", "Ahmedabad", "Gujarat"},
                {"CNB", "Kanpur Central", "Kanpur", "Uttar Pradesh"},
                {"PRYJ", "Prayagraj Junction", "Prayagraj", "Uttar Pradesh"},
                {"GZB", "Ghaziabad Junction", "Ghaziabad", "Uttar Pradesh"},
                {"MB", "Moradabad Junction", "Moradabad", "Uttar Pradesh"},
                {"BE", "Bareilly Junction", "Bareilly", "Uttar Pradesh"},
                {"KOTA", "Kota Junction", "Kota", "Rajasthan"},
                {"BRC", "Vadodara Junction", "Vadodara", "Gujarat"},
                {"ST", "Surat", "Surat", "Gujarat"},
                {"KPD", "Katpadi Junction", "Katpadi", "Tamil Nadu"}
        };

        for (String[] data : stationData) {
            String code = data[0];
            Station station = stationRepository.findByStationCode(code).orElseGet(() -> {
                Station s = new Station(code, data[1], data[2], data[3]);
                return stationRepository.save(s);
            });
            stationMap.put(code, station);
        }
        return stationMap;
    }

    private void initTrains(Map<String, Station> stations) {
        if (trainRepository.count() > 0) {
            return; // Trains already initialized
        }

        // 1. NDLS <-> BSB Vande Bharat Express
        createTrainWithClasses("22436", "New Delhi - Varanasi Vande Bharat Express", "Vande Bharat Express",
                stations.get("NDLS"), stations.get("BSB"),
                LocalTime.of(6, 0), LocalTime.of(14, 0), "08h 00m", "MON,TUE,WED,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 78, new BigDecimal("1750.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 52, new BigDecimal("3300.00")}
                });

        createTrainWithClasses("22435", "Varanasi - New Delhi Vande Bharat Express", "Vande Bharat Express",
                stations.get("BSB"), stations.get("NDLS"),
                LocalTime.of(15, 0), LocalTime.of(23, 0), "08h 00m", "MON,TUE,WED,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 78, new BigDecimal("1750.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 52, new BigDecimal("3300.00")}
                });

        // 2. NDLS <-> LKO Shatabdi Express
        createTrainWithClasses("12004", "Lucknow Swarna Shatabdi Express", "Shatabdi Express",
                stations.get("NDLS"), stations.get("LKO"),
                LocalTime.of(6, 10), LocalTime.of(12, 40), "06h 30m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 72, new BigDecimal("1165.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 46, new BigDecimal("1850.00")}
                });

        createTrainWithClasses("12003", "Lucknow - New Delhi Shatabdi Express", "Shatabdi Express",
                stations.get("LKO"), stations.get("NDLS"),
                LocalTime.of(15, 30), LocalTime.of(22, 15), "06h 45m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 72, new BigDecimal("1165.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 46, new BigDecimal("1850.00")}
                });

        // 3. NDLS <-> MMCT Tejas Rajdhani Express
        createTrainWithClasses("12952", "Mumbai Tejas Rajdhani Express", "Rajdhani Express",
                stations.get("NDLS"), stations.get("MMCT"),
                LocalTime.of(16, 55), LocalTime.of(8, 35), "15h 40m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.FIRST_AC, 24, new BigDecimal("4750.00")},
                        {TravelClass.SECOND_AC, 48, new BigDecimal("2850.00")},
                        {TravelClass.THIRD_AC, 64, new BigDecimal("2050.00")}
                });

        createTrainWithClasses("12951", "Mumbai - New Delhi Tejas Rajdhani Express", "Rajdhani Express",
                stations.get("MMCT"), stations.get("NDLS"),
                LocalTime.of(17, 0), LocalTime.of(8, 32), "15h 32m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.FIRST_AC, 24, new BigDecimal("4750.00")},
                        {TravelClass.SECOND_AC, 48, new BigDecimal("2850.00")},
                        {TravelClass.THIRD_AC, 64, new BigDecimal("2050.00")}
                });

        // 4. NDLS <-> HWH Rajdhani Express
        createTrainWithClasses("12302", "Howrah Rajdhani Express", "Rajdhani Express",
                stations.get("NDLS"), stations.get("HWH"),
                LocalTime.of(16, 50), LocalTime.of(9, 55), "17h 05m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.FIRST_AC, 24, new BigDecimal("4890.00")},
                        {TravelClass.SECOND_AC, 48, new BigDecimal("2930.00")},
                        {TravelClass.THIRD_AC, 64, new BigDecimal("2120.00")}
                });

        // 5. SBC <-> MAS Shatabdi Express
        createTrainWithClasses("12061", "Bengaluru - Chennai Shatabdi Express", "Shatabdi Express",
                stations.get("SBC"), stations.get("MAS"),
                LocalTime.of(6, 0), LocalTime.of(11, 0), "05h 00m", "MON,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 72, new BigDecimal("985.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 46, new BigDecimal("1650.00")}
                });

        createTrainWithClasses("12062", "Chennai - Bengaluru Shatabdi Express", "Shatabdi Express",
                stations.get("MAS"), stations.get("SBC"),
                LocalTime.of(17, 30), LocalTime.of(22, 30), "05h 00m", "MON,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 72, new BigDecimal("985.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 46, new BigDecimal("1650.00")}
                });

        // 6. JP <-> NDLS Double Decker Express
        createTrainWithClasses("12985", "Jaipur - Delhi Double Decker Express", "Express",
                stations.get("JP"), stations.get("NDLS"),
                LocalTime.of(6, 0), LocalTime.of(10, 25), "04h 25m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.AC_CHAIR_CAR, 120, new BigDecimal("500.00")},
                        {TravelClass.EXEC_CHAIR_CAR, 50, new BigDecimal("1000.00")}
                });

        // 7. PNBE <-> NDLS Sampoorna Kranti Express
        createTrainWithClasses("12393", "Sampoorna Kranti Superfast Express", "Express",
                stations.get("PNBE"), stations.get("NDLS"),
                LocalTime.of(19, 25), LocalTime.of(7, 55), "12h 30m", "MON,TUE,WED,THU,FRI,SAT,SUN",
                new Object[][]{
                        {TravelClass.FIRST_AC, 20, new BigDecimal("3400.00")},
                        {TravelClass.SECOND_AC, 48, new BigDecimal("2100.00")},
                        {TravelClass.THIRD_AC, 64, new BigDecimal("1490.00")},
                        {TravelClass.SLEEPER, 72, new BigDecimal("580.00")}
                });

        // 8. HWH <-> NDLS Duronto Express
        createTrainWithClasses("12260", "Sealdah - New Delhi AC Duronto Express", "Duronto Express",
                stations.get("HWH"), stations.get("NDLS"),
                LocalTime.of(20, 0), LocalTime.of(10, 30), "14h 30m", "MON,WED,THU,SUN",
                new Object[][]{
                        {TravelClass.FIRST_AC, 24, new BigDecimal("4600.00")},
                        {TravelClass.SECOND_AC, 48, new BigDecimal("2750.00")},
                        {TravelClass.THIRD_AC, 64, new BigDecimal("1950.00")}
                });

        log.info("Pre-initialized demonstration trains and seat configs.");
    }

    private void createTrainWithClasses(String trainNumber, String trainName, String trainType,
                                        Station source, Station destination,
                                        LocalTime dep, LocalTime arr, String duration, String days,
                                        Object[][] classConfigs) {
        Train train = new Train(trainNumber, trainName, trainType, source, destination, dep, arr, duration, days);
        Train savedTrain = trainRepository.save(train);

        for (Object[] cfg : classConfigs) {
            TravelClass travelClass = (TravelClass) cfg[0];
            int seats = (int) cfg[1];
            BigDecimal fare = (BigDecimal) cfg[2];
            TrainClassConfig classConfig = new TrainClassConfig(savedTrain, travelClass, seats, fare);
            classConfigRepository.save(classConfig);

            // Pre-seed seat inventory for today and the next 14 days
            LocalDate today = LocalDate.now();
            for (int d = 0; d < 15; d++) {
                LocalDate date = today.plusDays(d);
                if (train.runsOn(date.getDayOfWeek())) {
                    SeatInventory inventory = new SeatInventory(savedTrain, date, travelClass, seats);
                    seatInventoryRepository.save(inventory);
                }
            }
        }
    }

    private void initSchedules(Map<String, Station> stations) {
        if (trainScheduleRepository.count() > 0) {
            return;
        }

        // 1. NDLS -> BSB Vande Bharat (22436)
        trainRepository.findByTrainNumber("22436").ifPresent(train -> {
            addStop(train, 1, stations.get("NDLS"), null, LocalTime.of(6, 0), 0, 1);
            addStop(train, 2, stations.get("CNB"), LocalTime.of(10, 8), LocalTime.of(10, 10), 440, 1);
            addStop(train, 3, stations.get("PRYJ"), LocalTime.of(12, 8), LocalTime.of(12, 10), 635, 1);
            addStop(train, 4, stations.get("BSB"), LocalTime.of(14, 0), null, 759, 1);
        });

        // 2. BSB -> NDLS Vande Bharat (22435)
        trainRepository.findByTrainNumber("22435").ifPresent(train -> {
            addStop(train, 1, stations.get("BSB"), null, LocalTime.of(15, 0), 0, 1);
            addStop(train, 2, stations.get("PRYJ"), LocalTime.of(16, 30), LocalTime.of(16, 32), 124, 1);
            addStop(train, 3, stations.get("CNB"), LocalTime.of(18, 30), LocalTime.of(18, 32), 319, 1);
            addStop(train, 4, stations.get("NDLS"), LocalTime.of(23, 0), null, 759, 1);
        });

        // 3. NDLS -> LKO Shatabdi (12004)
        trainRepository.findByTrainNumber("12004").ifPresent(train -> {
            addStop(train, 1, stations.get("NDLS"), null, LocalTime.of(6, 10), 0, 1);
            addStop(train, 2, stations.get("GZB"), LocalTime.of(6, 48), LocalTime.of(6, 50), 26, 1);
            addStop(train, 3, stations.get("MB"), LocalTime.of(8, 46), LocalTime.of(8, 51), 167, 1);
            addStop(train, 4, stations.get("BE"), LocalTime.of(10, 13), LocalTime.of(10, 15), 258, 1);
            addStop(train, 5, stations.get("LKO"), LocalTime.of(12, 40), null, 512, 1);
        });

        // 4. LKO -> NDLS Shatabdi (12003)
        trainRepository.findByTrainNumber("12003").ifPresent(train -> {
            addStop(train, 1, stations.get("LKO"), null, LocalTime.of(15, 30), 0, 1);
            addStop(train, 2, stations.get("BE"), LocalTime.of(17, 35), LocalTime.of(17, 37), 235, 1);
            addStop(train, 3, stations.get("MB"), LocalTime.of(19, 10), LocalTime.of(19, 15), 326, 1);
            addStop(train, 4, stations.get("GZB"), LocalTime.of(21, 33), LocalTime.of(21, 35), 486, 1);
            addStop(train, 5, stations.get("NDLS"), LocalTime.of(22, 15), null, 512, 1);
        });

        // 5. NDLS -> MMCT Tejas Rajdhani (12952)
        trainRepository.findByTrainNumber("12952").ifPresent(train -> {
            addStop(train, 1, stations.get("NDLS"), null, LocalTime.of(16, 55), 0, 1);
            addStop(train, 2, stations.get("KOTA"), LocalTime.of(21, 30), LocalTime.of(21, 40), 465, 1);
            addStop(train, 3, stations.get("BRC"), LocalTime.of(3, 50), LocalTime.of(4, 0), 992, 2);
            addStop(train, 4, stations.get("ST"), LocalTime.of(5, 13), LocalTime.of(5, 18), 1122, 2);
            addStop(train, 5, stations.get("MMCT"), LocalTime.of(8, 35), null, 1386, 2);
        });

        // 6. MMCT -> NDLS Tejas Rajdhani (12951)
        trainRepository.findByTrainNumber("12951").ifPresent(train -> {
            addStop(train, 1, stations.get("MMCT"), null, LocalTime.of(17, 0), 0, 1);
            addStop(train, 2, stations.get("ST"), LocalTime.of(19, 43), LocalTime.of(19, 48), 263, 1);
            addStop(train, 3, stations.get("BRC"), LocalTime.of(21, 6), LocalTime.of(21, 16), 392, 1);
            addStop(train, 4, stations.get("KOTA"), LocalTime.of(3, 15), LocalTime.of(3, 20), 920, 2);
            addStop(train, 5, stations.get("NDLS"), LocalTime.of(8, 32), null, 1386, 2);
        });

        // 7. SBC -> MAS Shatabdi (12061)
        trainRepository.findByTrainNumber("12061").ifPresent(train -> {
            addStop(train, 1, stations.get("SBC"), null, LocalTime.of(6, 0), 0, 1);
            addStop(train, 2, stations.get("KPD"), LocalTime.of(9, 18), LocalTime.of(9, 20), 229, 1);
            addStop(train, 3, stations.get("MAS"), LocalTime.of(11, 0), null, 359, 1);
        });

        // 8. MAS -> SBC Shatabdi (12062)
        trainRepository.findByTrainNumber("12062").ifPresent(train -> {
            addStop(train, 1, stations.get("MAS"), null, LocalTime.of(17, 30), 0, 1);
            addStop(train, 2, stations.get("KPD"), LocalTime.of(19, 8), LocalTime.of(19, 10), 130, 1);
            addStop(train, 3, stations.get("SBC"), LocalTime.of(22, 30), null, 359, 1);
        });

        log.info("Initialized authentic demonstration train schedule halts.");
    }

    private void addStop(Train train, int stopNumber, Station station, LocalTime arr, LocalTime dep, int distanceKm, int dayCount) {
        if (station == null) return;
        TrainSchedule schedule = new TrainSchedule(train, stopNumber, station, arr, dep, distanceKm, dayCount);
        trainScheduleRepository.save(schedule);
    }
}
