package com.example.notesharing.service;

import com.example.notesharing.Repository.GroupMemberRepository;
import com.example.notesharing.Repository.NoteRepository;
import com.example.notesharing.Repository.SummaryRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.Note;
import com.example.notesharing.modal.Summary;
import com.example.notesharing.modal.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SummaryService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository memberRepository;

    @Autowired
    private AiService aiService;

    private String cleanHtml(String html) {
        return html
                .replaceAll("<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
    }
    public Summary createSummary(UUID noteId, String email) {

        // 1. Validate user
        userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // 2. Get note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new RuntimeException("Note not found")
                );

        // 3. Check access
        if (note.getGroup() != null) {

            // GROUP NOTE
            UUID groupId = note.getGroup().getId();

            memberRepository
                    .findByGroupIdAndUserEmail(groupId, email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "You are not a member of this group"
                            )
                    );

        } else {

            // PERSONAL NOTE
            if (!note.getUserEmail().equals(email)) {
                throw new RuntimeException(
                        "You are not allowed to summarize this note"
                );
            }
        }

        // 4. Check content
        if (note.getContent() == null ||
                note.getContent().isBlank()) {

            throw new RuntimeException(
                    "Note has no content to summarize"
            );
        }

        // 5. Clean HTML
        String cleanContent = cleanHtml(note.getContent());

        // 6. Generate summary
        String summaryText =
                aiService.summarize(cleanContent);

        // 7. Create summary
        Summary summary = summaryRepository
                .findByNoteId(noteId)
                .orElseGet(Summary::new);

        summary.setUserEmail(note.getUserEmail());
        summary.setTitle(note.getTitle());
        summary.setNote(note);
        summary.setSummaryContent(summaryText);

        if (summary.getCreatedAt() == null) {
            summary.setCreatedAt(LocalDateTime.now());
        }

        summary.setUpdatedAt(LocalDateTime.now());

        return summaryRepository.save(summary);
    }
//    public Summary createSummary(
//            UUID noteId,
//            String email
//    ) {
//
//        // 1. Validate user
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() ->
//                        new RuntimeException("User not found")
//                );
//
//        // 2. Get Note by ID
//        Note note = noteRepository.findById(noteId)
//                .orElseThrow(() ->
//                        new RuntimeException("Note not found")
//                );
//
//        // 3. Check ownership
//        if (!note.getUserEmail().equals(email)) {
//            throw new RuntimeException(
//                    "You are not allowed to summarize this note"
//            );
//        }
//
//        // 4. Check note content
//        if (note.getContent() == null ||
//                note.getContent().isBlank()) {
//
//            throw new RuntimeException(
//                    "Note has no content to summarize"
//            );
//        }
//
//        String cleanContent = cleanHtml(note.getContent());
//        // 5. Generate summary using AI
//        String summaryText =
//                aiService.summarize(cleanContent);
//
//        // 6. Create Summary
//        Summary summary = new Summary();
//
//        summary.setUserEmail(email);
//        summary.setTitle(note.getTitle());
//        summary.setNote(note);
//        summary.setSummaryContent(summaryText);
//
//        summary.setCreatedAt(LocalDateTime.now());
//        summary.setUpdatedAt(LocalDateTime.now());
//
//        // 7. Save
//        return summaryRepository.save(summary);
//    }

    public Summary getSummaryByNoteId(UUID noteId, String email) {

        // 1. Validate logged-in user
        userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // 2. Get note
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() ->
                        new RuntimeException("Note not found")
                );

        // 3. Check access
        if (note.getGroup() != null) {

            // =========================
            // GROUP NOTE
            // =========================

            UUID groupId = note.getGroup().getId();

            // User must be a member of the group
            memberRepository
                    .findByGroupIdAndUserEmail(groupId, email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "You are not a member of this group"
                            )
                    );

        } else {

            // =========================
            // PERSONAL NOTE
            // =========================

            if (!note.getUserEmail().equals(email)) {
                throw new RuntimeException(
                        "You are not allowed to access this summary"
                );
            }
        }

        // 4. Get the summary belonging to this NOTE
        return summaryRepository
                .findByNoteId(noteId)
                .orElseThrow(() ->
                        new RuntimeException("Summary not found")
                );
    }
//    public Summary getSummaryByNoteId(UUID noteId, String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() ->
//                        new RuntimeException("User not found")
//                );
//
//        Summary summary = summaryRepository.findByNoteId(noteId)
//                .orElseThrow(() ->
//                        new RuntimeException("Summary not found")
//                );
//
//        if (!summary.getUserEmail().equals(email)) {
//            throw new RuntimeException(
//                    "You are not allowed to access this summary"
//            );
//        }
//
//        return summary;
//    }

}
