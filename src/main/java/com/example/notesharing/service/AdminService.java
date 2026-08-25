package com.example.notesharing.service;

import com.example.notesharing.DTO.AdminAnalyticsDTO;
import com.example.notesharing.DTO.AdminDashboardDTO;
import com.example.notesharing.DTO.AdminMeDTO;
import com.example.notesharing.DTO.FeatureUsageDTO;
import com.example.notesharing.DTO.PageResponse;
import com.example.notesharing.DTO.UserAdminDTO;
import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Enum.CreditTransactionType;
import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Enum.SubscriptionStatus;
import com.example.notesharing.Repository.AiFeatureConfigRepository;
import com.example.notesharing.Repository.AiSubscriptionRepository;
import com.example.notesharing.Repository.CreditTransactionRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.AiFeatureConfig;
import com.example.notesharing.modal.AiSubscription;
import com.example.notesharing.modal.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read models + write operations behind the admin screens. Every metric has a real DB source; money
 * metrics (revenue, MRR) and un-instrumented ops metrics (avg processing time) are reported as
 * 0/empty rather than fabricated, since premium is unlocked with points, not real payments. All
 * user-scoped writes (status/credits/plan) delegate to {@link AiCreditService} so the "never
 * negative", history-preserving, tier-mirroring guarantees stay in one place.
 */
@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiSubscriptionRepository aiSubscriptionRepository;

    @Autowired
    private CreditTransactionRepository creditTransactionRepository;

    @Autowired
    private AiFeatureConfigRepository featureConfigRepository;

    @Autowired
    private AiCreditService aiCreditService;

    // ---- Dashboard / analytics -----------------------------------------------------------

    public AdminDashboardDTO dashboard() {
        long consumed = Math.abs(creditTransactionRepository.sumAmountByType(CreditTransactionType.CONSUME));
        return AdminDashboardDTO.builder()
                .totalUsers(userRepository.count())
                .aiCreditsUsed(consumed)
                .totalRevenue(0)
                .build();
    }

    public AdminAnalyticsDTO analytics(String range) {
        long consumed = Math.abs(creditTransactionRepository.sumAmountByType(CreditTransactionType.CONSUME));
        long active = aiSubscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long expired = aiSubscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
        long cancelled = aiSubscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long total = active + expired + cancelled;
        double churn = total > 0 ? ((double) (expired + cancelled) / total) * 100.0 : 0.0;

        return AdminAnalyticsDTO.builder()
                .mrr(0)
                .churnRate(round1(churn))
                .aiCreditsConsumed(consumed)
                .avgProcessingTime(0)
                .revenueBreakdown(List.of())
                .featureUsage(featureUsage())
                .build();
    }

    /** Real per-feature usage share from the CONSUME ledger. Feeds the dashboard bars + analytics donut. */
    private List<FeatureUsageDTO> featureUsage() {
        List<Object[]> rows = creditTransactionRepository.countByFeatureForType(CreditTransactionType.CONSUME);
        long total = 0;
        for (Object[] row : rows) {
            total += (Long) row[1];
        }
        List<FeatureUsageDTO> usage = new ArrayList<>();
        for (Object[] row : rows) {
            AiFeature feature = (AiFeature) row[0];
            long count = (Long) row[1];
            double percent = total > 0 ? ((double) count / total) * 100.0 : 0.0;
            String name = featureConfigRepository.findByFeature(feature)
                    .map(AiFeatureConfig::getDisplayName)
                    .orElse(feature.name());
            usage.add(FeatureUsageDTO.builder()
                    .feature(feature.name())
                    .name(name)
                    .count(count)
                    .percent(round1(percent))
                    .build());
        }
        return usage;
    }

    public AdminMeDTO me() {
        User user = userRepository.findByEmail(currentEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return AdminMeDTO.builder()
                .email(user.getEmail())
                .name(user.getUserName())
                .role(user.getRole().name())
                .build();
    }

    // ---- Users ----------------------------------------------------------------------------

    public PageResponse<UserAdminDTO> listUsers(String search, String accountType, String status,
                                                int page, int size) {
        String query = (search == null || search.isBlank()) ? null : search.trim();
        Boolean active = mapStatus(status);
        Boolean premium = mapAccountType(accountType);
        Pageable pageable = PageRequest.of(Math.max(0, page), size <= 0 ? 10 : size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.searchForAdmin(query, active, premium, pageable);

        List<User> users = result.getContent();
        List<String> emails = users.stream().map(User::getEmail).toList();
        Map<String, AiSubscription> subs = emails.isEmpty() ? Map.of()
                : aiSubscriptionRepository.findByUserEmailIn(emails).stream()
                        .collect(Collectors.toMap(AiSubscription::getUserEmail, s -> s, (a, b) -> a));

        List<UserAdminDTO> content = users.stream()
                .map(user -> toUserDTO(user, subs.get(user.getEmail())))
                .toList();

        return PageResponse.<UserAdminDTO>builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .build();
    }

    @Transactional
    public UserAdminDTO setUserStatus(UUID id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Boolean active = mapStatus(status);
        if (active == null) {
            throw new RuntimeException("Status must be Active or Suspended");
        }
        user.setActive(active);
        userRepository.save(user);
        return toUserDTO(user, aiSubscriptionRepository.findByUserEmail(user.getEmail()).orElse(null));
    }

    public UserAdminDTO adjustUserCredits(UUID id, int amount, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AiSubscription sub = aiCreditService.adjustCredits(user.getEmail(), amount, reason);
        return toUserDTO(user, sub);
    }

    public UserAdminDTO changeUserPlan(UUID id, UUID planConfigId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AiSubscription sub = aiCreditService.changeUserPlan(user.getEmail(), planConfigId);
        return toUserDTO(user, sub);
    }

    // ---- mapping helpers ------------------------------------------------------------------

    private UserAdminDTO toUserDTO(User user, AiSubscription sub) {
        boolean premium = sub != null
                && sub.getPlan() == SubscriptionPlan.PREMIUM
                && sub.getStatus() == SubscriptionStatus.ACTIVE;
        return UserAdminDTO.builder()
                .id(user.getId().toString())
                .name(user.getUserName())
                .email(user.getEmail())
                .accountType(premium ? "Premium" : "Free")
                .aiCredits(sub != null ? sub.getCurrentCredits() : 0)
                .joinedDate(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .status(user.isActive() ? "Active" : "Suspended")
                .build();
    }

    /** "Active" -> true, "Suspended" -> false, anything else (incl. "All Statuses") -> null (no filter). */
    private Boolean mapStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String value = status.trim();
        if (value.equalsIgnoreCase("Active")) {
            return true;
        }
        if (value.equalsIgnoreCase("Suspended")) {
            return false;
        }
        return null;
    }

    /** "Premium"/"Enterprise" -> true, "Free" -> false, anything else (incl. "All Types") -> null. */
    private Boolean mapAccountType(String accountType) {
        if (accountType == null || accountType.isBlank()) {
            return null;
        }
        String value = accountType.trim();
        if (value.equalsIgnoreCase("Free")) {
            return false;
        }
        if (value.equalsIgnoreCase("Premium") || value.equalsIgnoreCase("Enterprise")) {
            return true;
        }
        return null;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new RuntimeException("Unauthorized");
        }
        return auth.getName();
    }
}
