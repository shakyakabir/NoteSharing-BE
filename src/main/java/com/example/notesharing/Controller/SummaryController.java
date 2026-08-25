package com.example.notesharing.Controller;

import com.example.notesharing.DTO.RegisterRequest;
import com.example.notesharing.modal.Summary;
import com.example.notesharing.payload.ApiResponse;
import com.example.notesharing.service.AuthService;
import com.example.notesharing.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {




    @Autowired
    private SummaryService summaryService;


    @PostMapping()
    public ResponseEntity<Summary> createSummary(
            @RequestParam UUID noteId,
            @RequestParam String email
    ) {

        Summary summary =
                summaryService.createSummary(
                        noteId,
                        email
                );

        return ResponseEntity.ok(summary);
    }
    @GetMapping("/{noteId}")
    public ResponseEntity<Summary> getSummary(
            @PathVariable UUID noteId,
            @RequestParam String email
    ) {

        Summary summary =
                summaryService.getSummaryByNoteId(noteId, email);

        return ResponseEntity.ok(summary);
    }
}