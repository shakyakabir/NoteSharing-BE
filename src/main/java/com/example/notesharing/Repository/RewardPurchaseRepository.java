package com.example.notesharing.Repository;

import com.example.notesharing.modal.RewardPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardPurchaseRepository extends JpaRepository<RewardPurchase, UUID> {

    List<RewardPurchase> findByUserEmailOrderByPurchasedAtDesc(String userEmail);
}
