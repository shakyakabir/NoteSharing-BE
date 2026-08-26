package com.example.notesharing.service;

import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.Repository.UserScoreRepository;
import com.example.notesharing.modal.User;
import com.example.notesharing.modal.UserScore;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserScoreService {

    @Autowired
    private UserScoreRepository userScoreRepository;
    @Autowired
    private UserRepository userRepository;


    private User findUser(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

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
    @Transactional
    public long addPoints(String email, long points) {
        if (points <= 0) {
            return getTotalPoints(email);
        }

        UserScore userScore = userScoreRepository.findById(email)
                .orElseGet(() -> UserScore.builder().email(email).totalPoints(0L).build());

        userScore.setTotalPoints(userScore.getTotalPoints() + points);
        userScoreRepository.save(userScore);
        return userScore.getTotalPoints();
    }

    public long getTotalPoints(String email) {
        return userScoreRepository.findById(email)
                .map(UserScore::getTotalPoints)
                .orElse(0L);
    }
}

