package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin users-table row. Mirrors the frontend {@code User} shape (id, name, email, accountType,
 * aiCredits, joinedDate, status). {@code aiCredits} is the raw current balance (the frontend
 * formats it); {@code joinedDate} is the ISO-8601 creation timestamp (the frontend formats it);
 * {@code accountType} is "Premium" when the user holds an active PREMIUM subscription, else "Free".
 */
@Getter
@Setter
@Builder
public class UserAdminDTO {

    private String id;
    private String name;
    private String email;
    private String accountType;
    private int aiCredits;
    private String joinedDate;
    private String status;
}
