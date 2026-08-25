package com.example.notesharing.config;

import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Enum.UserRole;
import com.example.notesharing.Repository.AiFeatureConfigRepository;
import com.example.notesharing.Repository.SubscriptionPlanConfigRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.AiFeatureConfig;
import com.example.notesharing.modal.SubscriptionPlanConfig;
import com.example.notesharing.modal.User;
import com.example.notesharing.service.AiCreditPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Idempotent, non-destructive startup seeding. Runs on every boot but only fills gaps:
 * <ul>
 *   <li>Seeds the default Free + Premium {@link SubscriptionPlanConfig}s (allowance / refresh taken
 *       from {@link AiCreditPolicy} so the DB matches today's behaviour) if none exist.</li>
 *   <li>Seeds one {@link AiFeatureConfig} per {@link AiFeature} with {@code premiumOnly=false}
 *       (cost from the policy default) if none exist - so existing behaviour is preserved.</li>
 *   <li>Promotes every {@code app.admin.emails} user that already exists to {@link UserRole#ADMIN}.</li>
 * </ul>
 * Existing rows are never modified or deleted; users with no subscription row are still handled
 * lazily by {@code AiCreditService.getOrCreate}. No migration tool needed ({@code ddl-auto=update}).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    @Autowired
    private SubscriptionPlanConfigRepository planConfigRepository;

    @Autowired
    private AiFeatureConfigRepository featureConfigRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiCreditPolicy policy;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.admin.emails:}")
    private String adminEmails;

    @Override
    public void run(ApplicationArguments args) {
        seedPlanConfigs();
        seedFeatureConfigs();
        promoteAdmins();
    }

    private void seedPlanConfigs() {
        if (planConfigRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        SubscriptionPlanConfig free = SubscriptionPlanConfig.builder()
                .name("Free")
                .price(0)
                .period("mo")
                .tier(SubscriptionPlan.FREE)
                .creditAllowance(policy.getFreeCredits())
                .refreshDays(policy.getFreeRefreshDays())
                .features(featuresJson(
                        policy.getFreeCredits() + " AI credits", true,
                        "Refreshes every " + policy.getFreeRefreshDays() + " days", true,
                        "All AI features", true,
                        "Community access", true))
                .active(true)
                .sortOrder(0)
                .createdAt(now)
                .build();

        SubscriptionPlanConfig premium = SubscriptionPlanConfig.builder()
                .name("Premium")
                .price(0)
                .period("mo")
                .tier(SubscriptionPlan.PREMIUM)
                .creditAllowance(policy.getPremiumCredits())
                .refreshDays(policy.getPremiumRefreshDays())
                .features(featuresJson(
                        policy.getPremiumCredits() + " AI credits", true,
                        "Refreshes every " + policy.getPremiumRefreshDays() + " days", true,
                        "All AI features", true,
                        "Priority processing", true))
                .active(true)
                .sortOrder(1)
                .createdAt(now)
                .build();

        planConfigRepository.save(free);
        planConfigRepository.save(premium);
    }

    private void seedFeatureConfigs() {
        if (featureConfigRepository.count() > 0) {
            return;
        }
        for (AiFeature feature : AiFeature.values()) {
            featureConfigRepository.save(AiFeatureConfig.builder()
                    .feature(feature)
                    .cost(policy.defaultCostOf(feature))
                    .premiumOnly(false)
                    .status("ACTIVE")
                    .displayName(displayNameOf(feature))
                    .description(descriptionOf(feature))
                    .active(true)
                    .build());
        }
    }

    private void promoteAdmins() {
        if (adminEmails == null || adminEmails.isBlank()) {
            return;
        }
        for (String raw : adminEmails.split(",")) {
            String email = raw.trim();
            if (email.isEmpty()) {
                continue;
            }
            userRepository.findByEmail(email).ifPresent(user -> {
                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    userRepository.save(user);
                }
            });
        }
    }

    /** Build the pricing-card feature list JSON as an array of {@code {text, included}} objects. */
    private String featuresJson(Object... textIncludedPairs) {
        List<Map<String, Object>> features = new java.util.ArrayList<>();
        for (int i = 0; i + 1 < textIncludedPairs.length; i += 2) {
            features.add(Map.of(
                    "text", textIncludedPairs[i],
                    "included", textIncludedPairs[i + 1]));
        }
        try {
            return objectMapper.writeValueAsString(features);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String displayNameOf(AiFeature feature) {
        return switch (feature) {
            case SUMMARIZE -> "Summarize";
            case KEY_POINTS -> "Key Points";
            case REPORT -> "Report";
            case QUIZ -> "Quiz";
            case PPT -> "Presentation";
            case QA -> "Q&A";
        };
    }

    private String descriptionOf(AiFeature feature) {
        return switch (feature) {
            case SUMMARIZE -> "Condense notes into a concise summary.";
            case KEY_POINTS -> "Extract the key points from your notes.";
            case REPORT -> "Generate a structured report from your notes.";
            case QUIZ -> "Create a quiz from your notes.";
            case PPT -> "Generate presentation slides from your notes.";
            case QA -> "Ask questions and get answers from your notes.";
        };
    }
}
