package com.example.notesharing.modal;

import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Enum.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One row per user - holds the AI credit balance, the plan, the 21-day refresh schedule and the
 * (optional) premium subscription window. The backend is the sole source of truth for these
 * values; the frontend never writes them.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String userEmail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private int currentCredits;

    private int maxCredits;

    private LocalDateTime lastRefresh;

    private LocalDateTime nextRefresh;

    private LocalDateTime subscriptionStartDate;

    private LocalDateTime subscriptionEndDate;

    @Builder.Default
    private boolean autoRenew = false;
}
