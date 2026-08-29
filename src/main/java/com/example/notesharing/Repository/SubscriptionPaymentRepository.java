package com.example.notesharing.Repository;
import com.example.notesharing.Enum.PaymentMethod;
import com.example.notesharing.Enum.PaymentStatus;
import com.example.notesharing.modal.SubscriptionPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
public interface SubscriptionPaymentRepository
        extends JpaRepository<SubscriptionPayment, UUID> {

    Optional<SubscriptionPayment> findByTransactionUuid(
            String transactionUuid
    );

    Optional<SubscriptionPayment> findByPidx(
            String pidx
    );

    Optional<SubscriptionPayment> findByTransactionUuidAndStatus(
            String transactionUuid,
            PaymentStatus status
    );

    // ---- Admin revenue analytics / payment history ----------------------------------------

    /** Sum of payment amounts in a given status + method (COMPLETED eSewa = real subscription revenue). */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM SubscriptionPayment p " +
            "WHERE p.status = :status AND p.paymentMethod = :method")
    BigDecimal sumAmountByStatusAndMethod(@Param("status") PaymentStatus status,
                                          @Param("method") PaymentMethod method);

    /** Completed payments for a method, newest first - used to group revenue by month. */
    List<SubscriptionPayment> findByStatusAndPaymentMethodOrderByCreatedAtDesc(
            PaymentStatus status, PaymentMethod method);

    /** Paged payment history for a method (all statuses), newest first. */
    Page<SubscriptionPayment> findByPaymentMethodOrderByCreatedAtDesc(
            PaymentMethod method, Pageable pageable);
}
