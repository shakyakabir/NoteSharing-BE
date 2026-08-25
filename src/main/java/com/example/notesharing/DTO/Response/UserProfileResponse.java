package com.example.notesharing.DTO.Response;

import com.example.notesharing.Enum.SubscriptionTier;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {

    private UUID id;
    private String userName;
    private String email;
    private String bio;

    private int pointBalance;
    private int streakDays;

    // Subscription
    private SubscriptionTier subscriptionTier;
    private Instant subscriptionStartAt;
    private Instant subscriptionEndAt;

    // AI quota
    private int aiQuotaLimit;
    private int aiQuotaUsed;
    private int aiQuotaRemaining;
    private Instant aiQuotaExpires;

    private boolean active;
    private boolean emailVerified;
}