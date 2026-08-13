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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public PointBalanceDTO getBalance(String email) {
        User user = findUser(email);
        return PointBalanceDTO.builder()
                .email(user.getEmail())
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
        reward.setActive(true);
        reward.setCreatedAt(LocalDateTime.now());

        return rewardItemRepository.save(reward);
    }

    public List<RewardItem> getRewards() {
        return rewardItemRepository.findByActiveTrue();
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
}
