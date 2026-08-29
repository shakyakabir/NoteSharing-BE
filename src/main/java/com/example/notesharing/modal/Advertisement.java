package com.example.notesharing.modal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A sponsored ad served to FREE-tier users. Earnings follow a CPM + CPC model:
 * {@code impressions/1000 * cpmRate + clicks * cpcRate}, accumulated from the tracked
 * {@code impressions}/{@code clicks} counters. Premium users are ad-free and never see these.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private String targetUrl;

    /** Where the ad renders on the client: BANNER (default) | SIDEBAR | INLINE. */
    @Builder.Default
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'BANNER'")
    private String placement = "BANNER";

    /** Revenue earned per 1000 impressions (CPM). */
    @Builder.Default
    @Column(name = "cpm_rate", columnDefinition = "DOUBLE DEFAULT 0")
    private double cpmRate = 0;

    /** Revenue earned per click (CPC). */
    @Builder.Default
    @Column(name = "cpc_rate", columnDefinition = "DOUBLE DEFAULT 0")
    private double cpcRate = 0;

    @Builder.Default
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private long impressions = 0;

    @Builder.Default
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private long clicks = 0;

    /** Lifecycle status shown/edited in the admin Ads screen: ACTIVE | PAUSED | DRAFT. */
    @Builder.Default
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private String status = "ACTIVE";

    private boolean active;

    private LocalDateTime createdAt;
}
