package com.example.notesharing.service;

import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Enum.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for every tunable AI-credit value: per-feature costs, per-plan
 * allowances / refresh cadence, and the premium price + duration. Everything is injected via the
 * project's existing {@code @Value} idiom with sensible defaults, so the numbers can be changed in
 * application.properties without touching any call site (requirement: centralized, not hard-coded).
 */
@Component
public class AiCreditPolicy {

    @Value("${ai.cost.summarize:1}")
    private int costSummarize;

    @Value("${ai.cost.key-points:1}")
    private int costKeyPoints;

    @Value("${ai.cost.report:3}")
    private int costReport;

    @Value("${ai.cost.quiz:2}")
    private int costQuiz;

    @Value("${ai.cost.ppt:4}")
    private int costPpt;

    @Value("${ai.cost.qa:1}")
    private int costQa;

    @Value("${ai.plan.free.credits:20}")
    private int freeCredits;

    @Value("${ai.plan.free.refresh-days:21}")
    private int freeRefreshDays;

    @Value("${ai.plan.premium.credits:100}")
    private int premiumCredits;

    @Value("${ai.plan.premium.refresh-days:7}")
    private int premiumRefreshDays;

    @Value("${ai.premium.price-points:500}")
    private int premiumPricePoints;

    @Value("${ai.premium.duration-days:30}")
    private int premiumDurationDays;

    /** Credit cost of a single run of the given AI feature. */
    public int costOf(AiFeature feature) {
        return switch (feature) {
            case SUMMARIZE -> costSummarize;
            case KEY_POINTS -> costKeyPoints;
            case REPORT -> costReport;
            case QUIZ -> costQuiz;
            case PPT -> costPpt;
            case QA -> costQa;
        };
    }

    /** Allowance + refresh cadence for a plan. */
    public PlanConfig planConfig(SubscriptionPlan plan) {
        return switch (plan) {
            case PREMIUM -> new PlanConfig(premiumCredits, premiumRefreshDays);
            case FREE -> new PlanConfig(freeCredits, freeRefreshDays);
        };
    }

    public int getFreeCredits() {
        return freeCredits;
    }

    public int getFreeRefreshDays() {
        return freeRefreshDays;
    }

    public int getPremiumCredits() {
        return premiumCredits;
    }

    public int getPremiumRefreshDays() {
        return premiumRefreshDays;
    }

    public int getPremiumPricePoints() {
        return premiumPricePoints;
    }

    public int getPremiumDurationDays() {
        return premiumDurationDays;
    }

    /** Immutable pair of a plan's credit allowance and its refresh cadence (in days). */
    public record PlanConfig(int credits, int refreshDays) {
    }
}
