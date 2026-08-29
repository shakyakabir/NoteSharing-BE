package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One row of the admin eSewa payment-history table. Flattens {@link com.example.notesharing.modal.SubscriptionPayment}
 * (with the plan name resolved) so the frontend never touches the lazy plan association. Dates are
 * ISO strings, matching the {@code UserAdminDTO} convention.
 */
@Getter
@Setter
@Builder
public class PaymentHistoryDTO {

    private String id;
    private String userEmail;
    private String planName;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionUuid;
    private String createdAt;
    private String completedAt;
}
