package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.PresentationRequest;
import com.example.notesharing.DTO.Request.ReportRequest;
import com.example.notesharing.modal.GeneratedPresentation;
import com.example.notesharing.modal.GeneratedReport;
import com.example.notesharing.service.AiGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AiGenerationController {

    @Autowired
    private AiGenerationService aiGenerationService;

    @PostMapping("/presentations")
    public GeneratedPresentation createPresentation(@RequestBody PresentationRequest request) {
        return aiGenerationService.createPresentation(request);
    }

    @PostMapping("/presentations/from-note")
    public GeneratedPresentation createPresentationFromNote(@RequestBody PresentationRequest request) {
        return aiGenerationService.createPresentation(request);
    }

    @GetMapping("/presentations")
    public List<GeneratedPresentation> getPresentations(@RequestParam String email) {
        return aiGenerationService.getPresentations(email);
    }

    @GetMapping("/presentations/{id}")
    public GeneratedPresentation getPresentation(@PathVariable UUID id) {
        return aiGenerationService.getPresentation(id);
    }

    @GetMapping("/presentations/{id}/export")
    public String exportPresentation(@PathVariable UUID id) {
        return aiGenerationService.exportPresentation(id);
    }

    @PostMapping("/reports")
    public GeneratedReport createReport(@RequestBody ReportRequest request) {
        return aiGenerationService.createReport(request);
    }

    @PostMapping("/reports/summarize")
    public GeneratedReport summarize(@RequestBody ReportRequest request) {
        return aiGenerationService.createReport(request);
    }

    @GetMapping("/reports")
    public List<GeneratedReport> getReports(@RequestParam String email) {
        return aiGenerationService.getReports(email);
    }

    @GetMapping("/reports/{id}")
    public GeneratedReport getReport(@PathVariable UUID id) {
        return aiGenerationService.getReport(id);
    }
}
