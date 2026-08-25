package com.example.notesharing.modal;

import com.example.notesharing.Enum.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin-configurable subscription plan. Each plan carries its own credit allowance + refresh cadence
 * that <b>drives enforcement</b> ({@code AiCreditService.refreshIfDue} reads these values), plus the
 * enforcement {@code tier} it grants (custom Pro/Enterprise plans map to PREMIUM so every existing
 * {@code plan == PREMIUM} check keeps working). {@code price}/{@code period}/{@code features} are
 * display-only for the pricing UI. Non-destructive: seeded by DataSeeder, referenced (nullably) by
 * AiSubscription.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionPlanConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private double price;

    @Builder.Default
    private String period = "mo";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionPlan tier = SubscriptionPlan.FREE;

    private int creditAllowance;

    private int refreshDays;

    /** JSON array of {@code {"text": "...", "included": true}} for the pricing card feature list. */
    @Column(columnDefinition = "TEXT")
    private String features;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;

    @Builder.Default
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private int sortOrder = 0;

    private LocalDateTime createdAt;
}
