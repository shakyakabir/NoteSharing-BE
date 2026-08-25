package com.example.notesharing.modal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    private String userEmail;

    @ManyToOne
    private SubscriptionPlanConfig plan;

    private BigDecimal amount;

    private String paymentMethod;
    // ESEWA / KHALTI

    private String transactionId;

    private String providerTransactionId;

    private String status;
    // PENDING / COMPLETED / FAILED

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}