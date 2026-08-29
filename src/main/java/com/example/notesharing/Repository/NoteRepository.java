package com.example.notesharing.Repository;

import com.example.notesharing.Enum.Visibility;
import com.example.notesharing.modal.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {


    List<Note> findByUserEmail(String userEmail);
    Optional<Note> findByShareCode(String shareCode);

    List<Note> findByGroupId(UUID groupId);
    List<Note>findByVisibility(Visibility noteVisibility);
    List<Note> findByUserEmailOrVisibility(String userEmail, String visibility);
    Optional<Note> findFirstByGroupIdOrderByUpdatedAtDesc(UUID groupId);
    List<Note> findByUserEmailAndGroupIsNull(String userEmail);
}
