package com.example.notesharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Structured body carried in ApiResponse.data when an AI action is rejected for lack of credits,
 * so the frontend can react in code (show upgrade CTA) instead of parsing a message string.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InsufficientCreditsResponse {

    private String code;
    private int requiredCredits;
    private int availableCredits;
}
