package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read-only view of a user's subscription + the info the subscription page needs to render the
 * premium upgrade card (price in points, what premium grants, how many points the user has).
 */
@Getter
@Setter
@Builder
public class SubscriptionDTO {

    private String plan;
    private String status;
    private int currentCredits;
    private int maxCredits;
    private int refreshDays;
    private LocalDateTime nextRefresh;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private boolean autoRenew;

    // Info for the upgrade CTA (premium unlocked by spending existing points).
    private int pointBalance;
    private int premiumPricePoints;
    private int premiumDurationDays;
    private int premiumCredits;
    private int premiumRefreshDays;
}
