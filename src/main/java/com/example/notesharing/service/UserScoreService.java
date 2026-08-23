package com.example.notesharing.service;

import com.example.notesharing.Repository.UserScoreRepository;
import com.example.notesharing.modal.UserScore;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserScoreService {

    @Autowired
    private UserScoreRepository userScoreRepository;

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

