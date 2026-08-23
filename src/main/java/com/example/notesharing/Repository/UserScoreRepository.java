package com.example.notesharing.Repository;
import com.example.notesharing.modal.UserScore;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserScoreRepository extends JpaRepository<UserScore, String> {
}