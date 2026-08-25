package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Admin analytics payload. Real metrics: {@code churnRate} (expired+cancelled / total subscriptions)
 * and {@code aiCreditsConsumed} (from the CONSUME ledger), plus per-feature {@code featureUsage}.
 * Honest placeholders (no payment system / no timing instrumentation): {@code mrr} = 0,
 * {@code avgProcessingTime} = 0, {@code revenueBreakdown} = empty.
 */
@Getter
@Setter
@Builder
public class AdminAnalyticsDTO {

    private double mrr;
    private double churnRate;
    private long aiCreditsConsumed;
    private double avgProcessingTime;
    private List<RevenuePointDTO> revenueBreakdown;
    private List<FeatureUsageDTO> featureUsage;
}
