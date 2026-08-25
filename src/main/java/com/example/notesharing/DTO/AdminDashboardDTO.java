package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin dashboard stat cards: Total Users, AI Credits Used, Total Revenue. Revenue is 0 (premium is
 * unlocked with points, not real payments) - reported honestly rather than fabricated.
 */
@Getter
@Setter
@Builder
public class AdminDashboardDTO {

    private long totalUsers;
    private long aiCreditsUsed;
    private double totalRevenue;
}
