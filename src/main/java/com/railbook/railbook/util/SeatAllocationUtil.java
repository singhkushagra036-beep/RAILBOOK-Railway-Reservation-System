package com.railbook.railbook.util;

import com.railbook.railbook.model.BerthPreference;
import com.railbook.railbook.model.TravelClass;
import org.springframework.stereotype.Component;

@Component
public class SeatAllocationUtil {

    public static class SeatAllocationResult {
        private final String seatNumber;
        private final String allocatedBerth;

        public SeatAllocationResult(String seatNumber, String allocatedBerth) {
            this.seatNumber = seatNumber;
            this.allocatedBerth = allocatedBerth;
        }

        public String getSeatNumber() {
            return seatNumber;
        }

        public String getAllocatedBerth() {
            return allocatedBerth;
        }
    }

    /**
     * Allocates coach prefix, sequential seat index, and berth type.
     * Takes booked seat count (0-indexed current slot) and passenger berth preference.
     */
    public SeatAllocationResult allocateSeat(TravelClass travelClass, int slotIndex, BerthPreference preference) {
        String coachPrefix = switch (travelClass) {
            case FIRST_AC -> "H1";
            case SECOND_AC -> (slotIndex < 46) ? "A1" : "A2";
            case THIRD_AC -> (slotIndex < 64) ? "B1" : "B2";
            case SLEEPER -> (slotIndex < 72) ? "S1" : "S2";
            case AC_CHAIR_CAR -> (slotIndex < 75) ? "C1" : "C2";
            case EXEC_CHAIR_CAR -> "E1";
        };

        int seatInCoach = (slotIndex % 72) + 1;
        String berthType = determineBerthType(travelClass, seatInCoach, preference);

        return new SeatAllocationResult(coachPrefix + "-" + seatInCoach, berthType);
    }

    private String determineBerthType(TravelClass travelClass, int seatNumber, BerthPreference preference) {
        if (preference != null && preference != BerthPreference.NO_PREFERENCE) {
            return preference.getDisplayName();
        }

        if (travelClass == TravelClass.AC_CHAIR_CAR || travelClass == TravelClass.EXEC_CHAIR_CAR) {
            return (seatNumber % 2 == 1) ? "Window" : "Aisle";
        }

        int remainder = seatNumber % 8;
        return switch (remainder) {
            case 1, 4 -> "Lower Berth";
            case 2, 5 -> "Middle Berth";
            case 3, 6 -> "Upper Berth";
            case 7 -> "Side Lower";
            case 0 -> "Side Upper";
            default -> "Lower Berth";
        };
    }
}
