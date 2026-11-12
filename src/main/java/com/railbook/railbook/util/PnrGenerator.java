package com.railbook.railbook.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class PnrGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a unique 10-digit Indian Railway format PNR number.
     * The first digit is between 1-9 (usually 2, 4, 6, 8 indicating railway zones).
     */
    public String generatePnr() {
        // Leading digit between 2 and 8
        int firstDigit = 2 + RANDOM.nextInt(7);
        long remaining = Math.abs(RANDOM.nextLong()) % 1_000_000_000L;
        return String.format("%d%09d", firstDigit, remaining);
    }
}
