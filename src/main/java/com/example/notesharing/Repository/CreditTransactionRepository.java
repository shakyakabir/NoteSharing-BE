package com.example.notesharing.Repository;

import com.example.notesharing.modal.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    List<CreditTransaction> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
