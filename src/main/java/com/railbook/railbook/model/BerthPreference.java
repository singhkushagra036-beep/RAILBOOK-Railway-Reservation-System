package com.railbook.railbook.model;

public enum BerthPreference {
    NO_PREFERENCE("No Preference"),
    LOWER("Lower Berth"),
    MIDDLE("Middle Berth"),
    UPPER("Upper Berth"),
    SIDE_LOWER("Side Lower"),
    SIDE_UPPER("Side Upper"),
    WINDOW("Window Seat");

    private final String displayName;

    BerthPreference(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
