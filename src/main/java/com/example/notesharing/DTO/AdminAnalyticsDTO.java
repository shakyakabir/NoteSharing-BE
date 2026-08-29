package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Admin analytics payload. Revenue is real money: {@code subscriptionRevenue} is the completed eSewa
 * payment total, {@code adsRevenue} is the accumulated CPM+CPC ad earnings, and {@code totalRevenue}
 * is their sum. {@code aiCreditsConsumed} comes from the CONSUME ledger; {@code featureUsage} is the
 * per-feature share; {@code revenueBreakdown} is the last-6-months subscription+ads series.
 */
@Getter
@Setter
@Builder
public class AdminAnalyticsDTO {

    private double subscriptionRevenue;
    private double adsRevenue;
    private double totalRevenue;
    private long aiCreditsConsumed;
    private List<RevenuePointDTO> revenueBreakdown;
    private List<FeatureUsageDTO> featureUsage;
}
