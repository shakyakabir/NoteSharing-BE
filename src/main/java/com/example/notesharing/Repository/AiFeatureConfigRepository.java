package com.example.notesharing.Repository;

import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.modal.AiFeatureConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiFeatureConfigRepository extends JpaRepository<AiFeatureConfig, UUID> {

    Optional<AiFeatureConfig> findByFeature(AiFeature feature);

    List<AiFeatureConfig> findAllByOrderByFeatureAsc();
}
