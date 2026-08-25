package com.example.notesharing.Repository;

import com.example.notesharing.modal.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SummaryRepository extends JpaRepository<Summary, UUID> {

    Optional<Summary> findByNoteIdAndUserEmail(
            UUID noteId,
            String userEmail
    );

    List<Summary> findByUserEmailOrderByCreatedAtDesc(
            String userEmail
    );

    Optional<Summary> findByNoteId(UUID noteId);
}