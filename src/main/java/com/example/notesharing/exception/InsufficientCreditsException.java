package com.example.notesharing.exception;

/**
 * Thrown when a user tries to run an AI feature they cannot afford. Carries the numbers the
 * frontend needs so GlobalExceptionHandler can return a structured, machine-readable body.
 */
public class InsufficientCreditsException extends RuntimeException {

    private final int requiredCredits;
    private final int availableCredits;

    public InsufficientCreditsException(int requiredCredits, int availableCredits) {
        super("You do not have enough AI credits to perform this action.");
        this.requiredCredits = requiredCredits;
        this.availableCredits = availableCredits;
    }

    public int getRequiredCredits() {
        return requiredCredits;
    }

    public int getAvailableCredits() {
        return availableCredits;
    }
}
