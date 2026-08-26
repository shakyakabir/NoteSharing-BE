    package com.example.notesharing.service;
    
    import com.example.notesharing.DTO.PointBalanceDTO;
    import com.example.notesharing.DTO.Request.PointTransactionRequest;
    import com.example.notesharing.DTO.Request.RewardItemRequest;
    import com.example.notesharing.Enum.PointTransactionType;
    import com.example.notesharing.Repository.PointTransactionRepository;
    import com.example.notesharing.Repository.RewardItemRepository;
    import com.example.notesharing.Repository.RewardPurchaseRepository;
    import com.example.notesharing.Repository.UserRepository;
    import com.example.notesharing.modal.*;
    import jakarta.transaction.Transactional;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.UUID;
    
    @Service
    public class RewardService {
    
        @Autowired
        private UserRepository userRepository;
    
        @Autowired
        private PointTransactionRepository pointTransactionRepository;
    
        @Autowired
        private RewardItemRepository rewardItemRepository;
    
        @Autowired
        private RewardPurchaseRepository rewardPurchaseRepository;
        @Transactional
        public void recordActivity(String email) {
            User user = findUser(email);

            LocalDate today = LocalDate.now();
            LocalDate lastActivity = user.getLastSeenAt();

            // First activity ever
            if (lastActivity == null) {
                user.setStreakDays(1);
                user.setLastSeenAt(today);
                userRepository.save(user);
                return;
            }

            // Already recorded activity today
            if (lastActivity.equals(today)) {
                return;
            }

            // Activity yesterday -> continue streak
            if (lastActivity.equals(today.minusDays(1))) {
                user.setStreakDays(user.getStreakDays() + 1);
            } else {
                // Missed one or more days -> restart
                user.setStreakDays(1);
            }

            user.setLastSeenAt(today);
            userRepository.save(user);
        }
        public PointBalanceDTO getBalance(String email) {
            User user = findUser(email);
            return PointBalanceDTO.builder()
                    .email(user.getEmail())
                    .quizCount(user.getQuizCount())
                    .pointBalance(user.getPointBalance())
                    .streakDays(user.getStreakDays())
                    .aiQuotaUsed(user.getAiQuotaUsed())
                    .build();
        }
    
        public PointTransaction earnPoints(PointTransactionRequest request) {
            if (request == null) {
                throw new RuntimeException("Point transaction request is required");
            }
            if (request.getAmount() <= 0) {
                throw new RuntimeException("Point amount must be greater than zero");
            }
            User user = findUser(request.getUserEmail());
            user.setPointBalance(user.getPointBalance() + request.getAmount());
            userRepository.save(user);
    
            return saveTransaction(user, PointTransactionType.EARNED, request.getAmount(), request.getDescription());
        }
    
        public List<PointTransaction> getTransactions(String email) {
            return pointTransactionRepository.findByUserEmailOrderByCreatedAtDesc(email);
        }


        public RewardItem createReward(RewardItemRequest request) {
            if (request == null) {
                throw new RuntimeException("Reward request is required");
            }
            if (request.getCost() <= 0) {
                throw new RuntimeException("Reward cost must be greater than zero");
            }
    
            RewardItem reward = new RewardItem();
            reward.setTitle(required(request.getTitle(), "Reward title is required"));
            reward.setDescription(request.getDescription());
            reward.setCost(request.getCost());
            reward.setRewardType(request.getRewardType());
            reward.setAiCost(Math.max(0, request.getAiCost()));
            reward.setMaxUses(Math.max(0, request.getMaxUses()));
            String status = normalizeStatus(request.getStatus());
            reward.setStatus(status);
            reward.setActive("ACTIVE".equals(status));
            reward.setCreatedAt(LocalDateTime.now());
    
            return rewardItemRepository.save(reward);
        }
    
        public List<RewardItem> getRewards() {
            return rewardItemRepository.findByActiveTrue();
        }
    
        /** Admin: every reward regardless of status (self-service {@link #getRewards()} shows ACTIVE only). */
        public List<RewardItem> getAllRewards() {
            return rewardItemRepository.findAll();
        }
    
        /** Admin: edit an existing reward. Keeps {@code active} in sync with {@code status}. */
        public RewardItem updateReward(UUID id, RewardItemRequest request) {
            if (request == null) {
                throw new RuntimeException("Reward request is required");
            }
            RewardItem reward = rewardItemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reward not found"));
    
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                reward.setTitle(request.getTitle());
            }
            reward.setDescription(request.getDescription());
            if (request.getCost() > 0) {
                reward.setCost(request.getCost());
            }
            reward.setRewardType(request.getRewardType());
            reward.setAiCost(Math.max(0, request.getAiCost()));
            reward.setMaxUses(Math.max(0, request.getMaxUses()));
            String status = normalizeStatus(request.getStatus());
            reward.setStatus(status);
            reward.setActive("ACTIVE".equals(status));
    
            return rewardItemRepository.save(reward);
        }
    
        /** Admin: hard-delete a reward. */
        public void deleteReward(UUID id) {
            RewardItem reward = rewardItemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reward not found"));
            rewardItemRepository.delete(reward);
        }
    
        public RewardPurchase redeemReward(UUID rewardId, String email) {
            User user = findUser(email);
            RewardItem reward = rewardItemRepository.findById(rewardId)
                    .orElseThrow(() -> new RuntimeException("Reward not found"));
    
            if (!reward.isActive()) {
                throw new RuntimeException("Reward is not available");
            }
            if (user.getPointBalance() < reward.getCost()) {
                throw new RuntimeException("Insufficient points");
            }
    
            user.setPointBalance(user.getPointBalance() - reward.getCost());
            userRepository.save(user);
            saveTransaction(user, PointTransactionType.REDEEMED, -reward.getCost(), reward.getTitle());
    
            RewardPurchase purchase = new RewardPurchase();
            purchase.setUserEmail(email);
            purchase.setUser(user);
            purchase.setRewardItem(reward);
            purchase.setCost(reward.getCost());
            purchase.setPurchasedAt(LocalDateTime.now());
    
            return rewardPurchaseRepository.save(purchase);
        }
    
        public List<RewardPurchase> getPurchases(String email) {
            return rewardPurchaseRepository.findByUserEmailOrderByPurchasedAtDesc(email);
        }
    
        private PointTransaction saveTransaction(User user, PointTransactionType type, int amount, String description) {
            PointTransaction transaction = new PointTransaction();
            transaction.setUserEmail(user.getEmail());
            transaction.setUser(user);
            transaction.setType(type);
            transaction.setAmount(amount);
            transaction.setDescription(description);
            transaction.setCreatedAt(LocalDateTime.now());
            return pointTransactionRepository.save(transaction);
        }
    
        private User findUser(String email) {
            if (email == null || email.isBlank()) {
                throw new RuntimeException("Email is required");
            }
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
    
        private String required(String value, String message) {
            if (value == null || value.isBlank()) {
                throw new RuntimeException(message);
            }
            return value;
        }
    
        /** Normalize a reward status to one of ACTIVE | DRAFT | SUSPENDED, defaulting to ACTIVE. */
        private String normalizeStatus(String status) {
            if (status == null || status.isBlank()) {
                return "ACTIVE";
            }
            String normalized = status.trim().toUpperCase();
            return switch (normalized) {
                case "ACTIVE", "DRAFT", "SUSPENDED" -> normalized;
                default -> "ACTIVE";
            };
        }
    }
