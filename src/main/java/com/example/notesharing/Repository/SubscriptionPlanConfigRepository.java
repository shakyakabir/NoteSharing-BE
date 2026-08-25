package com.example.notesharing.Repository;

import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.modal.SubscriptionPlanConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanConfigRepository extends JpaRepository<SubscriptionPlanConfig, UUID> {

    List<SubscriptionPlanConfig> findByActiveTrueOrderBySortOrderAsc();

    Optional<SubscriptionPlanConfig> findByNameIgnoreCase(String name);

    /** The default plan for a tier - used for lazy FREE creation and premium-expiry downgrade. */
    Optional<SubscriptionPlanConfig> findFirstByTierAndActiveTrueOrderBySortOrderAsc(SubscriptionPlan tier);

    long countByTier(SubscriptionPlan tier);
}
