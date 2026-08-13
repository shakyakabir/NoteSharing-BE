package com.example.notesharing.Repository;

import com.example.notesharing.modal.RewardItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RewardItemRepository extends JpaRepository<RewardItem, UUID> {

    List<RewardItem> findByActiveTrue();
}
