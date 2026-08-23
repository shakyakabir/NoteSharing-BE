package com.example.notesharing.Enum;

public enum QuizMode {

    SOLO,
    COLLABORATIVE;

    public static QuizMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return SOLO;
        }
        try {
            return QuizMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SOLO;
        }
    }
}
