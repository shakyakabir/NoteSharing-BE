package com.example.notesharing.modal;

import com.example.notesharing.Enum.PaymentMethod;
import com.example.notesharing.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlanConfig plan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // Our internal transaction ID
    @Column(nullable = false, unique = true)
    private String transactionUuid;

    // eSewa transaction code / Khalti transaction ID
    private String providerTransactionId;

    // Khalti pidx
    private String pidx;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
