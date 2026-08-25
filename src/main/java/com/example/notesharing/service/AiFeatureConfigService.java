package com.example.notesharing.service;

import com.example.notesharing.DTO.AiFeatureConfigDTO;
import com.example.notesharing.DTO.Request.FeatureCostItemRequest;
import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Repository.AiFeatureConfigRepository;
import com.example.notesharing.modal.AiFeatureConfig;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Admin read + bulk save for {@link AiFeatureConfig} (the "AI Credit Costs" screen). Costs edited
 * here become the source of truth that {@code AiCreditPolicy.costOf} reads, so nothing is hard-coded
 * per controller. Rows are keyed by the stable {@code feature} enum name; unknown/absent features are
 * skipped rather than created (the seeded set is authoritative).
 */
@Service
public class AiFeatureConfigService {

    @Autowired
    private AiFeatureConfigRepository featureConfigRepository;

    public List<AiFeatureConfigDTO> list() {
        return featureConfigRepository.findAllByOrderByFeatureAsc()
                .stream().map(this::toDTO).toList();
    }

    /** Bulk save from the admin screen's "Save Changes"; applies only the fields present on each row. */
    @Transactional
    public List<AiFeatureConfigDTO> bulkUpdate(List<FeatureCostItemRequest> items) {
        if (items != null) {
            for (FeatureCostItemRequest item : items) {
                if (item == null || item.getFeature() == null || item.getFeature().isBlank()) {
                    continue;
                }
                AiFeature feature;
                try {
                    feature = AiFeature.valueOf(item.getFeature().trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    continue;
                }
                AiFeatureConfig config = featureConfigRepository.findByFeature(feature).orElse(null);
                if (config == null) {
                    continue;
                }
                if (item.getCost() != null) {
                    config.setCost(Math.max(0, item.getCost()));
                }
                if (item.getPremiumOnly() != null) {
                    config.setPremiumOnly(item.getPremiumOnly());
                }
                if (item.getStatus() != null && !item.getStatus().isBlank()) {
                    config.setStatus(item.getStatus().trim().toUpperCase());
                }
                featureConfigRepository.save(config);
            }
        }
        return list();
    }

    private AiFeatureConfigDTO toDTO(AiFeatureConfig config) {
        return AiFeatureConfigDTO.builder()
                .id(config.getId().toString())
                .feature(config.getFeature().name())
                .name(config.getDisplayName())
                .description(config.getDescription())
                .status(config.getStatus())
                .cost(config.getCost())
                .premiumOnly(config.isPremiumOnly())
                .build();
    }
}
