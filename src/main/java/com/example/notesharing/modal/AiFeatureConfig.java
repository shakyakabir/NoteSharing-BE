package com.example.notesharing.modal;

import com.example.notesharing.Enum.AiFeature;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Admin-configurable per-feature AI settings: credit {@code cost} (the DB source of truth that
 * {@code AiCreditPolicy.costOf} prefers over its {@code @Value} fallback) and {@code premiumOnly}
 * access gating (enforced server-side in {@code AiCreditService.consume}). {@code displayName},
 * {@code description} and {@code status} feed the admin AI-credits screen. Seeded one row per
 * AiFeature by DataSeeder with {@code premiumOnly=false}, so existing behaviour is preserved.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiFeatureConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private AiFeature feature;

    private int cost;

    @Builder.Default
    @Column(name = "premium_only", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean premiumOnly = false;

    /** Display status shown on the admin screen: ACTIVE | BETA. */
    @Builder.Default
    private String status = "ACTIVE";

    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(columnDefinition = "BOOLEAN DEFAULT true")
    private boolean active = true;
}
