package com.example.notesharing.modal;

import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Enum.CreditTransactionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Append-only ledger of every AI credit movement (consume / refund / refresh / grant) - mirrors
 * PointTransaction. `amount` is negative for a consume and positive for a grant/refund/refresh.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    private AiFeature feature;

    @Enumerated(EnumType.STRING)
    private CreditTransactionType type;

    private int amount;

    private int balanceAfter;

    private String description;

    private LocalDateTime createdAt;
}
