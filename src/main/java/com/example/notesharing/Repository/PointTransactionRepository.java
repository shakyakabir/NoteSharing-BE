package com.example.notesharing.Repository;

import com.example.notesharing.modal.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, UUID> {

    List<PointTransaction> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
