package com.example.notesharing.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;
    private String sourceContent;
    private String content;
    private String title;
    private String reportType;         // SUMMARY | KEY_POINTS | REPORT (default)

    // --- fields coming from PromptSection / ConfigurationSummary ---
    private String prompt;             // free-text goal from PromptSection
    private Integer detailLevel;       // 1 Brief, 2 Balanced, 3 Detailed
    private String writingStyle;       // "Professional / Academic" | "Casual / Conversational" | "Technical / Concise"

    // --- field coming from ReferenceReportSection ---
    private String referenceContent;   // extracted text of the uploaded reference report, if any

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "note_id")
    @JsonIgnore
    private Note sourceNote;

}
