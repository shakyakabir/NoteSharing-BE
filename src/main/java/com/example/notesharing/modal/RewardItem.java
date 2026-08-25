package com.example.notesharing.modal;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RewardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private int cost;

    private String rewardType;

    @Builder.Default
    @Column(name = "ai_cost", columnDefinition = "INT DEFAULT 0")
    private int aiCost = 0;

    @Builder.Default
    @Column(name = "max_uses", columnDefinition = "INT DEFAULT 0")
    private int maxUses = 0;

    /** Lifecycle status shown/edited in the admin Point Shop: ACTIVE | DRAFT | SUSPENDED. */
    @Builder.Default
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private String status = "ACTIVE";

    private boolean active;

    private LocalDateTime createdAt;
}
