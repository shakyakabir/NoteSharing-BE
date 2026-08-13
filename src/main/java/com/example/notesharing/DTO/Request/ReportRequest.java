package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReportRequest {
    private String userEmail;
    private UUID noteId;
    private String sourceContent;      // overwritten by controller after text extraction, if a file was uploaded
    private String title;
    private String reportType;         // SUMMARY | KEY_POINTS | REPORT (default)

    // --- fields coming from PromptSection / ConfigurationSummary ---
    private String prompt;             // free-text goal from PromptSection
    private Integer detailLevel;       // 1 Brief, 2 Balanced, 3 Detailed
    private String writingStyle;       // "Professional / Academic" | "Casual / Conversational" | "Technical / Concise"

    // --- field coming from ReferenceReportSection ---
    private String referenceContent;
//    private String title;
//    private String userEmail;
//    private UUID noteId;
//    private String sourceContent;
//    private String reportType;
}
