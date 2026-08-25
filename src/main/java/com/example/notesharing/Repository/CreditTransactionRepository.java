package com.example.notesharing.Repository;

import com.example.notesharing.Enum.CreditTransactionType;
import com.example.notesharing.modal.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    List<CreditTransaction> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    /** Signed sum of all ledger amounts of a type (CONSUME amounts are negative). Used by admin analytics. */
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CreditTransaction c WHERE c.type = :type")
    long sumAmountByType(@Param("type") CreditTransactionType type);

    /** Per-feature transaction counts for a ledger type: rows of {@code [AiFeature, Long count]}. */
    @Query("SELECT c.feature, COUNT(c) FROM CreditTransaction c " +
            "WHERE c.type = :type AND c.feature IS NOT NULL GROUP BY c.feature")
    List<Object[]> countByFeatureForType(@Param("type") CreditTransactionType type);
}
