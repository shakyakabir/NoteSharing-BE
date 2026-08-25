package com.example.notesharing.Repository;
import com.example.notesharing.Enum.PaymentStatus;
import com.example.notesharing.modal.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
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
}