package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.CreateNoteRequest;
import com.example.notesharing.DTO.Request.JoinShareRequest;
import com.example.notesharing.DTO.Request.UpdateNoteRequest;
import com.example.notesharing.Repository.CollaborationGroupRepository;
import com.example.notesharing.Repository.GroupMemberRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.CollaborationGroup;
import com.example.notesharing.modal.GroupMember;
import com.example.notesharing.modal.Note;
import com.example.notesharing.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
                              @RequestBody UpdateNoteRequest request,
                              @RequestParam String email) {
        return noteService.updateContent(id, request.getContent(),request.getTitle(), email);
    }


    @GetMapping
    public List<Note> getAllNotes(@RequestParam String email) {
        return noteService.getAllNotes(email);
    }

    @GetMapping("/public")
    public List<Note> getAllPublicNotes() {
        return noteService.getAllPublicNotes();
    }
    @GetMapping("/public/{id}")
    public Note get(@PathVariable UUID id) {
        return noteService.getPublicNote(id);
    }

//    @PostMapping("/group/{groupId}")
//    public Note createGroupNote(@RequestBody CreateNoteRequest req,
//                                @PathVariable UUID groupId,
//                                @RequestParam String email) {
//        return noteService.createGroupNote(req, groupId, email);
//    }

    @PutMapping("/group/{groupId}")
    public ResponseEntity<Note> updateGroupNote(
            @PathVariable UUID groupId,
            @RequestBody CreateNoteRequest req,
            @RequestParam String email
    ) {
        Note updated = noteService.updateGroupNote(groupId, req, email);
        return ResponseEntity.ok(updated);
    }
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<Note> getLatestGroupNote(
            @PathVariable UUID groupId,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(noteService.getLatestGroupNote(groupId, email));
    }


}
