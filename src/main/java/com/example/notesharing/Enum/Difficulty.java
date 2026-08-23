package com.example.notesharing.Enum;

public enum Difficulty {
    BEGINNER(10),
    INTERMEDIATE(50),
    ADVANCED(100),
    EXPERT(250);

    private final int points;

    Difficulty(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public static Difficulty fromString(String value) {
        if (value == null || value.isBlank()) {
            return INTERMEDIATE;
        }
        try {
            return Difficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return INTERMEDIATE;
        }
    }
}

