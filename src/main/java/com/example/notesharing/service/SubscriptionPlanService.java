package com.example.notesharing.service;

import com.example.notesharing.DTO.PlanFeatureDTO;
import com.example.notesharing.DTO.Request.PlanRequest;
import com.example.notesharing.DTO.SubscriptionPlanDTO;
import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Repository.AiSubscriptionRepository;
import com.example.notesharing.Repository.SubscriptionPlanConfigRepository;
import com.example.notesharing.exception.ApiCodedException;
import com.example.notesharing.modal.SubscriptionPlanConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class SubscriptionPlanService {

    @Autowired
    private SubscriptionPlanConfigRepository planConfigRepository;

    @Autowired
    private AiSubscriptionRepository aiSubscriptionRepository;

    @Autowired
    private AiCreditPolicy policy;

    @Autowired
    private ObjectMapper objectMapper;

    /** Active plans in display order - shared by the admin grid and the public pricing surface. */
    public List<SubscriptionPlanDTO> listActive() {
        return planConfigRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream().map(this::toDTO).toList();
    }

    public SubscriptionPlanDTO create(PlanRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Plan name is required");
        }
        SubscriptionPlan tier = resolveTier(request.getTier(), request.getName());
        AiCreditPolicy.PlanConfig defaults = policy.planConfig(tier);
        int allowance = request.getCreditAllowance() != null
                ? Math.max(0, request.getCreditAllowance()) : defaults.credits();
        int refreshDays = request.getRefreshDays() != null ? request.getRefreshDays() : defaults.refreshDays();

        SubscriptionPlanConfig plan = SubscriptionPlanConfig.builder()
                .name(request.getName().trim())
                .price(request.getPrice() != null ? request.getPrice() : 0)
                .period(request.getPeriod() != null && !request.getPeriod().isBlank()
                        ? request.getPeriod().trim() : "mo")
                .tier(tier)
                .creditAllowance(allowance)
                .refreshDays(refreshDays)
                .features(writeFeatures(request.getFeatures()))
                .active(request.getActive() == null || request.getActive())
                .sortOrder(request.getSortOrder() != null
                        ? request.getSortOrder() : (int) planConfigRepository.count())
                .createdAt(LocalDateTime.now())
                .build();
        return toDTO(planConfigRepository.save(plan));
    }

    public SubscriptionPlanDTO update(UUID id, PlanRequest request) {
        SubscriptionPlanConfig plan = planConfigRepository.findById(id)
                .orElseThrow(ApiCodedException::invalidPlan);
        if (request.getName() != null && !request.getName().isBlank()) {
            plan.setName(request.getName().trim());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getPeriod() != null && !request.getPeriod().isBlank()) {
            plan.setPeriod(request.getPeriod().trim());
        }
        if (request.getTier() != null && !request.getTier().isBlank()) {
            plan.setTier(resolveTier(request.getTier(), plan.getName()));
        }
        if (request.getCreditAllowance() != null) {
            plan.setCreditAllowance(Math.max(0, request.getCreditAllowance()));
        }
        if (request.getRefreshDays() != null) {
            plan.setRefreshDays(request.getRefreshDays());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(writeFeatures(request.getFeatures()));
        }
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }
        return toDTO(planConfigRepository.save(plan));
    }

    public void delete(UUID id) {
        SubscriptionPlanConfig plan = planConfigRepository.findById(id)
                .orElseThrow(ApiCodedException::invalidPlan);
        if (aiSubscriptionRepository.countByPlanConfig_Id(id) > 0) {
            plan.setActive(false);
            planConfigRepository.save(plan);
        } else {
            planConfigRepository.delete(plan);
        }
    }

    // ---- mapping helpers ------------------------------------------------------------------

    /** FREE only when explicitly "FREE"/named "Free"; unknown custom labels map to PREMIUM-grade. */
    private SubscriptionPlan resolveTier(String tier, String name) {
        if (tier != null && !tier.isBlank()) {
            try {
                return SubscriptionPlan.valueOf(tier.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return SubscriptionPlan.PREMIUM;
            }
        }
        return (name != null && name.trim().equalsIgnoreCase("free"))
                ? SubscriptionPlan.FREE : SubscriptionPlan.PREMIUM;
    }

    private String writeFeatures(List<PlanFeatureDTO> features) {
        if (features == null || features.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(features);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<PlanFeatureDTO> readFeatures(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PlanFeatureDTO>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private SubscriptionPlanDTO toDTO(SubscriptionPlanConfig plan) {
        return SubscriptionPlanDTO.builder()
                .id(plan.getId().toString())
                .name(plan.getName())
                .price(plan.getPrice())
                .period(plan.getPeriod())
                .tier(plan.getTier().name())
                .creditAllowance(plan.getCreditAllowance())
                .refreshDays(plan.getRefreshDays())
                .features(readFeatures(plan.getFeatures()))
                .active(plan.isActive())
                .sortOrder(plan.getSortOrder())
                .build();
    }
}
