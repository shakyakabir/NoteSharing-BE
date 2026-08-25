package com.example.notesharing.Repository;

import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Enum.SubscriptionStatus;
import com.example.notesharing.modal.AiSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiSubscriptionRepository extends JpaRepository<AiSubscription, UUID> {

    Optional<AiSubscription> findByUserEmail(String userEmail);

    /**
     * Race-safe deduction: the guarded UPDATE only succeeds while the row still has enough
     * credits, so two concurrent requests can never both spend the last credit. Returns the
     * number of rows changed - 0 means "insufficient credits" (or a lost race). flushAutomatically
     * pushes any pending refresh before the decrement; clearAutomatically drops the stale entity
     * so the follow-up read sees the new balance.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AiSubscription s SET s.currentCredits = s.currentCredits - :cost " +
            "WHERE s.userEmail = :email AND s.currentCredits >= :cost")
    int tryConsume(@Param("email") String email, @Param("cost") int cost);

    /**
     * Refund path (AI generation failed after the charge) - unconditional atomic increment.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AiSubscription s SET s.currentCredits = s.currentCredits + :amount " +
            "WHERE s.userEmail = :email")
    int incrementCredits(@Param("email") String email, @Param("amount") int amount);

    // ---- Admin analytics / dashboard aggregates -------------------------------------------

    long countByPlan(SubscriptionPlan plan);

    long countByStatus(SubscriptionStatus status);

    /** How many subscriptions reference a plan - decides soft- vs hard-delete when an admin removes it. */
    long countByPlanConfig_Id(UUID planConfigId);

    /** Batch-load subscriptions for a page of users (avoids an N+1 join in the admin users list). */
    List<AiSubscription> findByUserEmailIn(List<String> userEmails);
}
