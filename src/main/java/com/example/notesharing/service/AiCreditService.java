package com.example.notesharing.service;

import com.example.notesharing.DTO.CreditStatusDTO;
import com.example.notesharing.DTO.SubscriptionDTO;
import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Enum.CreditTransactionType;
import com.example.notesharing.Enum.PointTransactionType;
import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Enum.SubscriptionStatus;
import com.example.notesharing.Repository.AiSubscriptionRepository;
import com.example.notesharing.Repository.CreditTransactionRepository;
import com.example.notesharing.Repository.PointTransactionRepository;
import com.example.notesharing.Repository.SubscriptionPlanConfigRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.exception.ApiCodedException;
import com.example.notesharing.exception.InsufficientCreditsException;
import com.example.notesharing.modal.AiSubscription;
import com.example.notesharing.modal.CreditTransaction;
import com.example.notesharing.modal.PointTransaction;
import com.example.notesharing.modal.SubscriptionPlanConfig;
import com.example.notesharing.modal.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Owns all AI credit + subscription logic. The backend is the sole source of truth: the identity
 * is always taken from the JWT session (never a request param), balances live only in the DB, and
 * deductions go through a race-safe atomic UPDATE. Credit logic is kept separate from AI
 * generation - the AI services simply call {@link #consume} before generating and {@link #refund}
 * if generation fails.
 */
@Service
public class AiCreditService {

    @Autowired
    private AiSubscriptionRepository aiSubscriptionRepository;

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiCreditPolicy policy;

    @Autowired
    private SubscriptionPlanConfigRepository planConfigRepository;

    // ---- Public read API (identity from JWT session) --------------------------------------

    @Transactional
    public CreditStatusDTO status() {
        AiSubscription sub = refreshIfDue(getOrCreate(currentEmail()));
        return toCreditStatus(sub);
    }

    @Transactional
    public SubscriptionDTO subscription() {
        String email = currentEmail();
        AiSubscription sub = refreshIfDue(getOrCreate(email));
        int pointBalance = userRepository.findByEmail(email).map(User::getPointBalance).orElse(0);
        return toSubscription(sub, pointBalance);
    }

    public List<CreditTransaction> usage() {
        return creditTransactionRepository.findByUserEmailOrderByCreatedAtDesc(currentEmail());
    }

    public Map<String, Integer> featureCosts() {
        Map<String, Integer> costs = new LinkedHashMap<>();
        for (AiFeature feature : AiFeature.values()) {
            costs.put(feature.name(), policy.costOf(feature));
        }
        return costs;
    }

    // ---- Credit consumption (called from the AI generation services) ----------------------

    /**
     * Charge the JWT-authenticated user for one run of {@code feature}. Refreshes first if due,
     * then deducts atomically. Throws {@link InsufficientCreditsException} (never overdraws) when
     * the balance is too low.
     */
    @Transactional
    public void consume(AiFeature feature) {
        String email = currentEmail();
        AiSubscription sub = refreshIfDue(getOrCreate(email));
        User user = sub.getUser();

        // Feature-access gate (backend-enforced): premium-only features require the PREMIUM tier.
        if (policy.isPremiumOnly(feature) && sub.getPlan() != SubscriptionPlan.PREMIUM) {
            throw ApiCodedException.featureNotAvailable();
        }

        int cost = policy.costOf(feature);

        int rows = aiSubscriptionRepository.tryConsume(email, cost);
        if (rows == 0) {
            int available = aiSubscriptionRepository.findByUserEmail(email)
                    .map(AiSubscription::getCurrentCredits)
                    .orElse(0);
            throw new InsufficientCreditsException(cost, available);
        }

        int balanceAfter = aiSubscriptionRepository.findByUserEmail(email)
                .map(AiSubscription::getCurrentCredits)
                .orElse(0);
        saveCreditTransaction(email, user, feature, CreditTransactionType.CONSUME, -cost, balanceAfter,
                feature.name() + " (-" + cost + " credits)");
    }

    /**
     * Give the credits back when AI generation fails after the charge, so a failed request never
     * costs the user (reservation semantics). Runs in its own transaction.
     */
    @Transactional
    public void refund(AiFeature feature) {
        String email = currentEmail();
        int cost = policy.costOf(feature);
        aiSubscriptionRepository.incrementCredits(email, cost);

        int balanceAfter = aiSubscriptionRepository.findByUserEmail(email)
                .map(AiSubscription::getCurrentCredits)
                .orElse(0);
        User user = userRepository.findByEmail(email).orElse(null);
        saveCreditTransaction(email, user, feature, CreditTransactionType.REFUND, cost, balanceAfter,
                feature.name() + " refund (+" + cost + " credits)");
    }

    // ---- Premium upgrade (spend existing points) ------------------------------------------

    @Transactional
    public SubscriptionDTO upgradeToPremiumWithPoints() {
        String email = currentEmail();
        AiSubscription sub = refreshIfDue(getOrCreate(email));

        int price = policy.getPremiumPricePoints();
        int rows = userRepository.trySpendPoints(email, price);
        if (rows == 0) {
            throw new RuntimeException("Insufficient points to unlock Premium");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        savePointTransaction(user, PointTransactionType.REDEEMED, -price, "Premium subscription");

        SubscriptionPlanConfig premiumConfig = planConfigRepository
                .findFirstByTierAndActiveTrueOrderBySortOrderAsc(SubscriptionPlan.PREMIUM).orElse(null);
        int credits = premiumConfig != null ? premiumConfig.getCreditAllowance() : policy.getPremiumCredits();
        int refreshDays = premiumConfig != null ? premiumConfig.getRefreshDays() : policy.getPremiumRefreshDays();
        LocalDateTime now = LocalDateTime.now();
        sub.setPlanConfig(premiumConfig);
        sub.setPlan(SubscriptionPlan.PREMIUM);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setMaxCredits(credits);
        sub.setCurrentCredits(credits);
        sub.setLastRefresh(now);
        sub.setNextRefresh(now.plusDays(refreshDays));
        sub.setSubscriptionStartDate(now);
        sub.setSubscriptionEndDate(now.plusDays(policy.getPremiumDurationDays()));
        aiSubscriptionRepository.save(sub);

        saveCreditTransaction(email, user, null, CreditTransactionType.GRANT, credits,
                credits, "Premium activated");

        return toSubscription(sub, user.getPointBalance());
    }

    // ---- Admin operations (target user by email; the admin identity is enforced by Spring Security) ----

    /**
     * Admin: move a user onto a specific plan. Resets the balance to the new allowance (a plan
     * change is a fresh grant) and recomputes the schedule; the append-only ledger preserves the
     * full history. Sets / clears the premium window from the plan's tier. Never negative.
     */
    @Transactional
    public AiSubscription changeUserPlan(String email, UUID planConfigId) {
        AiSubscription sub = refreshIfDue(getOrCreate(email));
        SubscriptionPlanConfig config = planConfigRepository.findById(planConfigId)
                .orElseThrow(ApiCodedException::invalidPlan);

        LocalDateTime now = LocalDateTime.now();
        sub.setPlanConfig(config);
        sub.setPlan(config.getTier());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setMaxCredits(config.getCreditAllowance());
        sub.setCurrentCredits(config.getCreditAllowance());
        sub.setLastRefresh(now);
        sub.setNextRefresh(config.getRefreshDays() > 0 ? now.plusDays(config.getRefreshDays()) : null);
        if (config.getTier() == SubscriptionPlan.PREMIUM) {
            sub.setSubscriptionStartDate(now);
            sub.setSubscriptionEndDate(now.plusDays(policy.getPremiumDurationDays()));
        } else {
            sub.setSubscriptionStartDate(null);
            sub.setSubscriptionEndDate(null);
        }
        aiSubscriptionRepository.save(sub);

        saveCreditTransaction(email, sub.getUser(), null, CreditTransactionType.GRANT,
                config.getCreditAllowance(), config.getCreditAllowance(),
                "Plan changed to " + config.getName());
        return sub;
    }

    /**
     * Admin: adjust a user's balance by {@code delta} (may be negative). Clamps at 0 so the balance
     * is never negative, and logs an ADJUST ledger entry with the amount actually applied.
     */
    @Transactional
    public AiSubscription adjustCredits(String email, int delta, String reason) {
        AiSubscription sub = refreshIfDue(getOrCreate(email));
        int updated = Math.max(0, sub.getCurrentCredits() + delta);
        int applied = updated - sub.getCurrentCredits();
        sub.setCurrentCredits(updated);
        aiSubscriptionRepository.save(sub);

        saveCreditTransaction(email, sub.getUser(), null, CreditTransactionType.ADJUST, applied, updated,
                (reason == null || reason.isBlank()) ? "Admin credit adjustment" : reason);
        return sub;
    }

    // ---- Internals ------------------------------------------------------------------------

    /**
     * Get the caller's subscription, lazily creating a FREE one on first touch. This covers both
     * brand-new signups and users that pre-date the feature - no migration or backfill needed.
     */
    private AiSubscription getOrCreate(String email) {
        return aiSubscriptionRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    SubscriptionPlanConfig freeConfig = planConfigRepository
                            .findFirstByTierAndActiveTrueOrderBySortOrderAsc(SubscriptionPlan.FREE).orElse(null);
                    int credits = freeConfig != null ? freeConfig.getCreditAllowance()
                            : policy.planConfig(SubscriptionPlan.FREE).credits();
                    int refreshDays = freeConfig != null ? freeConfig.getRefreshDays()
                            : policy.planConfig(SubscriptionPlan.FREE).refreshDays();
                    LocalDateTime now = LocalDateTime.now();
                    AiSubscription sub = AiSubscription.builder()
                            .userEmail(email)
                            .user(user)
                            .plan(SubscriptionPlan.FREE)
                            .planConfig(freeConfig)
                            .status(SubscriptionStatus.ACTIVE)
                            .currentCredits(credits)
                            .maxCredits(credits)
                            .lastRefresh(now)
                            .nextRefresh(now.plusDays(refreshDays))
                            .autoRenew(false)
                            .build();
                    return aiSubscriptionRepository.save(sub);
                });
    }

    /**
     * Server-authoritative, scheduler-free refresh computed on read. Handles (1) premium expiry ->
     * downgrade to FREE, then (2) catch-up refresh: advance the schedule in whole refresh-periods
     * until nextRefresh is in the future (so weeks/months offline still land on one reset), setting
     * credits back to the full allowance. Never accumulates.
     */
    private AiSubscription refreshIfDue(AiSubscription sub) {
        LocalDateTime now = LocalDateTime.now();
        boolean changed = false;

        if (sub.getPlan() == SubscriptionPlan.PREMIUM
                && sub.getSubscriptionEndDate() != null
                && now.isAfter(sub.getSubscriptionEndDate())) {
            // Premium window elapsed -> downgrade to the default FREE plan (config-driven, else policy).
            SubscriptionPlanConfig freeConfig = planConfigRepository
                    .findFirstByTierAndActiveTrueOrderBySortOrderAsc(SubscriptionPlan.FREE).orElse(null);
            int freeCredits = freeConfig != null ? freeConfig.getCreditAllowance()
                    : policy.planConfig(SubscriptionPlan.FREE).credits();
            int freeRefreshDays = freeConfig != null ? freeConfig.getRefreshDays()
                    : policy.planConfig(SubscriptionPlan.FREE).refreshDays();
            sub.setPlanConfig(freeConfig);
            sub.setPlan(SubscriptionPlan.FREE);
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setMaxCredits(freeCredits);
            if (sub.getCurrentCredits() > freeCredits) {
                sub.setCurrentCredits(freeCredits);
            }
            sub.setLastRefresh(now);
            sub.setNextRefresh(freeRefreshDays > 0 ? now.plusDays(freeRefreshDays) : null);
            changed = true;
        }

        int refreshDays = refreshDaysOf(sub);
        boolean refreshed = false;
        if (refreshDays <= 0) {
            // Non-refreshing plan (e.g. an unlimited / one-off config): leave the schedule untouched.
            if (sub.getNextRefresh() != null) {
                sub.setNextRefresh(null);
                changed = true;
            }
        } else if (sub.getNextRefresh() == null) {
            sub.setLastRefresh(now);
            sub.setNextRefresh(now.plusDays(refreshDays));
            changed = true;
        } else {
            while (!sub.getNextRefresh().isAfter(now)) { // nextRefresh <= now
                sub.setLastRefresh(sub.getNextRefresh());
                sub.setNextRefresh(sub.getNextRefresh().plusDays(refreshDays));
                refreshed = true;
            }
        }

        if (refreshed) {
            sub.setCurrentCredits(sub.getMaxCredits());
            changed = true;
        }

        if (changed) {
            aiSubscriptionRepository.save(sub);
            if (refreshed) {
                saveCreditTransaction(sub.getUserEmail(), sub.getUser(), null,
                        CreditTransactionType.REFRESH, sub.getMaxCredits(), sub.getMaxCredits(),
                        "Credits refreshed");
            }
        }
        return sub;
    }

    /** Refresh cadence (days) for a subscription: its plan config when set, else the policy default. */
    private int refreshDaysOf(AiSubscription sub) {
        return sub.getPlanConfig() != null
                ? sub.getPlanConfig().getRefreshDays()
                : policy.planConfig(sub.getPlan()).refreshDays();
    }

    private void saveCreditTransaction(String userEmail, User user, AiFeature feature,
                                       CreditTransactionType type, int amount, int balanceAfter,
                                       String description) {
        CreditTransaction transaction = CreditTransaction.builder()
                .userEmail(userEmail)
                .user(user)
                .feature(feature)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        creditTransactionRepository.save(transaction);
    }

    private void savePointTransaction(User user, PointTransactionType type, int amount, String description) {
        PointTransaction transaction = new PointTransaction();
        transaction.setUserEmail(user.getEmail());
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(transaction);
    }

    private CreditStatusDTO toCreditStatus(AiSubscription sub) {
        return CreditStatusDTO.builder()
                .plan(sub.getPlan().name())
                .status(sub.getStatus().name())
                .currentCredits(sub.getCurrentCredits())
                .maxCredits(sub.getMaxCredits())
                .refreshDays(refreshDaysOf(sub))
                .nextRefresh(sub.getNextRefresh())
                .daysUntilRefresh(daysUntil(sub.getNextRefresh()))
                .build();
    }

    private SubscriptionDTO toSubscription(AiSubscription sub, int pointBalance) {
        return SubscriptionDTO.builder()
                .plan(sub.getPlan().name())
                .status(sub.getStatus().name())
                .currentCredits(sub.getCurrentCredits())
                .maxCredits(sub.getMaxCredits())
                .refreshDays(refreshDaysOf(sub))
                .nextRefresh(sub.getNextRefresh())
                .subscriptionStartDate(sub.getSubscriptionStartDate())
                .subscriptionEndDate(sub.getSubscriptionEndDate())
                .autoRenew(sub.isAutoRenew())
                .pointBalance(pointBalance)
                .premiumPricePoints(policy.getPremiumPricePoints())
                .premiumDurationDays(policy.getPremiumDurationDays())
                .premiumCredits(policy.getPremiumCredits())
                .premiumRefreshDays(policy.getPremiumRefreshDays())
                .build();
    }

    private long daysUntil(LocalDateTime target) {
        if (target == null) {
            return 0;
        }
        return Math.max(ChronoUnit.DAYS.between(LocalDateTime.now(), target), 0);
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()
                || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Unauthorized");
        }
        return auth.getName();
    }
}
