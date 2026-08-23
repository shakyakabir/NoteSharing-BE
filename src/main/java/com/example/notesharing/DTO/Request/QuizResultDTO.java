package com.example.notesharing.DTO.Request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultDTO {
    private int score;
    private int total;
    /** Points actually credited to the player. Always 0 for COLLABORATIVE mode. */
    private long pointsEarned;
    private String mode;
}
