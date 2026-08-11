package com.example.notesharing.service;

import com.example.notesharing.DTO.Request.CreateNoteRequest;
import com.example.notesharing.Enum.Visibility;
import com.example.notesharing.Repository.NoteRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.Note;
import com.example.notesharing.modal.User;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
public class NoteService {

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    UserRepository userRepository;

    // CREATE NOTE (EMPTY CONTENT)
    public Note createNote(CreateNoteRequest req, String email) {

        Note note = new Note();

        note.setTitle(req.getTitle());
        note.setContent(""); // IMPORTANT: empty at start
        note.setUserEmail(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        note.setUser(user);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        Visibility visibility = Visibility.valueOf(req.getVisibility());
        note.setVisibility(visibility);

        if (visibility == Visibility.FRIENDS) {
            note.setShareCode(UUID.randomUUID().toString().substring(0, 8));
        }

        return noteRepository.save(note);
    }

    // UPDATE CONTENT (AUTO SAVE)
    public Note updateContent(UUID id, String content, String title,String email) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        // permission check
        if (note.getVisibility() == Visibility.PRIVATE &&
                !note.getUserEmail().equals(email)) {
            throw new RuntimeException("Not allowed");
        }

        note.setContent(content);
        note.setTitle(title);
        note.setUpdatedAt(LocalDateTime.now());

        return noteRepository.save(note);
    }

    // GET NOTE
    public Note getNote(UUID id, String email) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (note.getVisibility() == Visibility.PRIVATE &&
                !note.getUserEmail().equals(email)) {
            throw new RuntimeException("Not allowed");
        }

        return note;
    }

    public Note getPublicNote(UUID id) {

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));




        return note;
    }

    // JOIN SHARE
    public Note joinSharedNote(String shareCode, String email) {

        return noteRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new RuntimeException("Invalid code"));
    }


    public List<Note> getAllNotes(String email) {
        return noteRepository.findByUserEmail(email);
    }

    public List<Note> getAllPublicNotes() {
        return noteRepository.findByVisibility(Visibility.PUBLIC);
    }

}
