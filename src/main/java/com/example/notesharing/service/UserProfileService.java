package com.example.notesharing.service;

import com.example.notesharing.DTO.Response.UserProfileResponse;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.User;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        int remaining =
                Math.max(
                        0,
                        user.getAiQuotaLimit() - user.getAiQuotaUsed()
                );

        return UserProfileResponse.builder()

                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .bio(user.getBio())

                .pointBalance(user.getPointBalance())
                .streakDays(user.getStreakDays())

                // Subscription
                .subscriptionTier(
                        user.getSubscriptionTier()
                )
                .subscriptionStartAt(
                        user.getSubscriptionStartAt()
                )
                .subscriptionEndAt(
                        user.getSubscriptionEndAt()
                )

                // AI quota
                .aiQuotaLimit(
                        user.getAiQuotaLimit()
                )
                .aiQuotaUsed(
                        user.getAiQuotaUsed()
                )
                .aiQuotaRemaining(
                        remaining
                )
                .aiQuotaExpires(
                        user.getAiQuotaExpires()
                )

                .active(user.isActive())
                .emailVerified(user.isEmailVerified())

                .build();
    }
}