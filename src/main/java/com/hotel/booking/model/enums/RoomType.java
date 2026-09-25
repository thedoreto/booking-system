package com.hotel.booking.model.enums;

public enum RoomType {
    SINGLE("Единична стая"),
    DOUBLE("Двойна стая"),
    APARTMENT("Апартамент");

    // Име за показване в UI и в AI асистента
    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
