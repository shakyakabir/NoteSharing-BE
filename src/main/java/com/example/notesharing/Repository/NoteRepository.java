package com.example.notesharing.Repository;

import com.example.notesharing.modal.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {


    List<Note> findByUserEmail(String userEmail);
    Optional<Note> findByShareCode(String shareCode);
}
