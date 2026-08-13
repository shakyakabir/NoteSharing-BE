package com.example.notesharing.Repository;

import com.example.notesharing.modal.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findByUserEmail(String userEmail);
}
