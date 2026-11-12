package com.railbook.railbook.model;

public enum TravelClass {
    FIRST_AC("1A", "AC First Class (1A)"),
    SECOND_AC("2A", "AC 2 Tier (2A)"),
    THIRD_AC("3A", "AC 3 Tier (3A)"),
    SLEEPER("SL", "Sleeper Class (SL)"),
    AC_CHAIR_CAR("CC", "AC Chair Car (CC)"),
    EXEC_CHAIR_CAR("EC", "Exec Chair Car (EC)");

    private final String code;
    private final String displayName;

    TravelClass(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TravelClass fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (TravelClass tc : TravelClass.values()) {
            if (tc.getCode().equalsIgnoreCase(code) || tc.name().equalsIgnoreCase(code)) {
                return tc;
            }
        }
        return null;
    }
}
