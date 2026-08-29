package com.example.notesharing.service;

import com.example.notesharing.DTO.AdminAnalyticsDTO;
import com.example.notesharing.DTO.AdminDashboardDTO;
import com.example.notesharing.DTO.AdminMeDTO;
import com.example.notesharing.DTO.FeatureUsageDTO;
import com.example.notesharing.DTO.PageResponse;
import com.example.notesharing.DTO.PaymentHistoryDTO;
import com.example.notesharing.DTO.RevenuePointDTO;
import com.example.notesharing.DTO.UserAdminDTO;
import com.example.notesharing.Enum.AiFeature;
import com.example.notesharing.Enum.CreditTransactionType;
import com.example.notesharing.Enum.PaymentMethod;
import com.example.notesharing.Enum.PaymentStatus;
import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Enum.SubscriptionStatus;
import com.example.notesharing.Repository.AiFeatureConfigRepository;
import com.example.notesharing.Repository.AiSubscriptionRepository;
import com.example.notesharing.Repository.CreditTransactionRepository;
import com.example.notesharing.Repository.SubscriptionPaymentRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.AiFeatureConfig;
import com.example.notesharing.modal.AiSubscription;
import com.example.notesharing.modal.SubscriptionPayment;
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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read models + write operations behind the admin screens. Every metric has a real DB source:
 * subscription revenue from completed eSewa payments, ad revenue from the CPM+CPC counters, AI
 * credits from the CONSUME ledger. All user-scoped writes (status/credits/plan) delegate to
 * {@link AiCreditService} so the "never negative", history-preserving, tier-mirroring guarantees
 * stay in one place.
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

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    @Autowired
    private AdvertisementService advertisementService;

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

        // Real revenue: completed eSewa payments (Khalti has no completion callback yet, so it never
        // contributes) plus the accumulated CPM+CPC ad earnings.
        double subscriptionRevenue = subscriptionPaymentRepository
                .sumAmountByStatusAndMethod(PaymentStatus.COMPLETED, PaymentMethod.ESEWA)
                .doubleValue();
        double adsRevenue = advertisementService.totalAdRevenue();
        double totalRevenue = subscriptionRevenue + adsRevenue;

        return AdminAnalyticsDTO.builder()
                .subscriptionRevenue(round1(subscriptionRevenue))
                .adsRevenue(round1(adsRevenue))
                .totalRevenue(round1(totalRevenue))
                .aiCreditsConsumed(consumed)
                .revenueBreakdown(revenueBreakdown(adsRevenue))
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

    /**
     * Last-6-months revenue series. {@code subscription} is grouped from completed eSewa payments by
     * their {@code createdAt} month. Ad counters are not timestamped, so the whole accumulated
     * {@code adsRevenue} is placed in the current (most recent) month bucket rather than fabricating a
     * per-month split - an intentional approximation.
     */
    private List<RevenuePointDTO> revenueBreakdown(double adsRevenue) {
        YearMonth current = YearMonth.now();
        List<YearMonth> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }

        Map<YearMonth, Double> subsByMonth = new HashMap<>();
        List<SubscriptionPayment> completed = subscriptionPaymentRepository
                .findByStatusAndPaymentMethodOrderByCreatedAtDesc(PaymentStatus.COMPLETED, PaymentMethod.ESEWA);
        for (SubscriptionPayment payment : completed) {
            if (payment.getCreatedAt() == null || payment.getAmount() == null) {
                continue;
            }
            subsByMonth.merge(YearMonth.from(payment.getCreatedAt()), payment.getAmount().doubleValue(), Double::sum);
        }

        DateTimeFormatter label = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        List<RevenuePointDTO> series = new ArrayList<>();
        for (YearMonth month : months) {
            double subscription = subsByMonth.getOrDefault(month, 0.0);
            double ads = month.equals(current) ? adsRevenue : 0.0;
            series.add(RevenuePointDTO.builder()
                    .month(month.atDay(1).format(label))
                    .subscription(round1(subscription))
                    .ads(round1(ads))
                    .build());
        }
        return series;
    }

    /** Paged eSewa payment history for the admin screen (all statuses, newest first). */
    @Transactional
    public PageResponse<PaymentHistoryDTO> paymentHistory(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size <= 0 ? 10 : size);
        Page<SubscriptionPayment> result = subscriptionPaymentRepository
                .findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod.ESEWA, pageable);

        List<PaymentHistoryDTO> content = result.getContent().stream()
                .map(this::toPaymentDTO)
                .toList();

        return PageResponse.<PaymentHistoryDTO>builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .page(result.getNumber())
                .size(result.getSize())
                .build();
    }

    private PaymentHistoryDTO toPaymentDTO(SubscriptionPayment payment) {
        return PaymentHistoryDTO.builder()
                .id(payment.getId() != null ? payment.getId().toString() : null)
                .userEmail(payment.getUserEmail())
                .planName(payment.getPlan() != null ? payment.getPlan().getName() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .transactionUuid(payment.getTransactionUuid())
                .createdAt(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : null)
                .completedAt(payment.getCompletedAt() != null ? payment.getCompletedAt().toString() : null)
                .build();
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
