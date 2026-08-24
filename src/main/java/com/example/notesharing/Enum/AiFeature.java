package com.example.notesharing.Enum;

public enum AiFeature {
    SUMMARIZE,
    KEY_POINTS,
    REPORT,
    QUIZ,
    PPT,
    QA;

    /**
     * Maps a GeneratedReport.reportType (SUMMARY / KEY_POINTS / anything else) to the AI feature
     * that is charged for it - mirrors the switch in AiGenerationService.createReport.
     */
    public static AiFeature fromReportType(String reportType) {
        if (reportType == null || reportType.isBlank()) {
            return REPORT;
        }
        return switch (reportType.trim().toUpperCase()) {
            case "SUMMARY" -> SUMMARIZE;
            case "KEY_POINTS" -> KEY_POINTS;
            default -> REPORT;
        };
    }
}
