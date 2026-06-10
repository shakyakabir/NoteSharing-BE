package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.CreateNoteRequest;
import com.example.notesharing.DTO.Request.JoinShareRequest;
import com.example.notesharing.modal.Note;
import com.example.notesharing.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    // 1. CREATE NOTE (ONLY METADATA)
    @PostMapping
    public Note create(@RequestBody CreateNoteRequest req,
                       @RequestParam String email) {
        return noteService.createNote(req, email);
    }

    // 2. GET NOTE
    @GetMapping("/{id}")
    public Note get(@PathVariable UUID id,
                    @RequestParam String email) {
        return noteService.getNote(id, email);
    }

    // 3. UPDATE CONTENT (AUTO-SAVE FROM TIPPY)
    @PutMapping("/{id}")
    public Note updateContent(@PathVariable UUID     id,
                              @RequestBody String content,
                              @RequestParam String email) {
        return noteService.updateContent(id, content, email);
    }

    // 4. JOIN SHARE
    @PostMapping("/join")
    public Note join(@RequestBody JoinShareRequest req) {
        return noteService.joinSharedNote(req.getShareCode(), req.getUserEmail());
    }

    @GetMapping
    public List<Note> getAllNotes(@RequestParam String email) {
        return noteService.getAllNotes(email);
    }
}