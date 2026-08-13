package com.example.notesharing.service;

import com.example.notesharing.DTO.Request.PresentationRequest;
import com.example.notesharing.DTO.Request.ReportRequest;
import com.example.notesharing.DTO.Response.Presentation.PresentationContent;
import com.example.notesharing.DTO.Response.Presentation.SlideContent;
import com.example.notesharing.Repository.GeneratedPresentationRepository;
import com.example.notesharing.Repository.GeneratedReportRepository;
import com.example.notesharing.Repository.NoteRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AiGenerationService {

    @Autowired
    private GeneratedPresentationRepository presentationRepository;

    @Autowired
    private GeneratedReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Autowired
    private ObjectMapper objectMapper; // com.fasterxml.jackson.databind.ObjectMapper

    public GeneratedPresentation createPresentation(PresentationRequest request) {
        if (request == null) {
            throw new RuntimeException("Presentation request is required");
        }
        validateEmail(request.getUserEmail());

        User user = findUser(request.getUserEmail());
        Note note = findNote(request.getNoteId());
        String source = resolveSource(request.getSourceContent(), note);

        int slideCount = resolveSlideCount(request.getSlideCount());
        String theme = defaultValue(request.getTheme(), "Clean");
        String template = defaultValue(request.getTemplateName(), "Study Deck");
        PresentationContent aiContent = generateStructuredContent(source, slideCount, theme,template);

        GeneratedPresentation presentation = new GeneratedPresentation();
        presentation.setTitle(defaultValue(request.getTitle(), aiContent.title()));
        presentation.setUserEmail(request.getUserEmail());
        presentation.setUser(user);
        presentation.setSourceNote(note);
        presentation.setSourceContent(source);
        presentation.setTheme(theme);
        presentation.setTemplateName(defaultValue(request.getTemplateName(), "Study Deck"));
        presentation.setCreatedAt(LocalDateTime.now());
        presentation.setUpdatedAt(LocalDateTime.now());
        boolean includeImages = request.getIncludeImages() == null || request.getIncludeImages();

        int order = 1;
//        for (SlideContent slideContent : aiContent.slides()) {
//            String imageUrl = imageGenerationService.generateImage(slideContent.image());
//
//            PresentationSlide slide = PresentationSlide.builder()
//                    .title(slideContent.title())
//                    .content(buildSlideBody(slideContent))
//                    .slideOrder(order++)
//                    .design(theme)
//                    .imageUrl(imageUrl)
//                    .imagePrompt(slideContent.image())
//                    .build();
//
//            slide.setPresentation(presentation);
//            presentation.getSlides().add(slide);
        for (SlideContent slideContent : aiContent.slides()) {

            String imageUrl = null;
            String imagePrompt = null;

            if (slideContent.image() != null
                    && slideContent.image().required()
                    && slideContent.image().prompt() != null
                    && !slideContent.image().prompt().isBlank()) {

                imagePrompt = slideContent.image().prompt();
                imageUrl = imageGenerationService.generateImage(imagePrompt);
            }

            PresentationSlide slide = PresentationSlide.builder()
                    .title(slideContent.title())
                    .subtitle(slideContent.subTitle())
                    .content(slideContent.content())
                    .bullets(slideContent.bullets() != null ? slideContent.bullets() : new ArrayList<>())
                    .slideOrder(order++)
                    .design(theme)
                    .slideType(slideContent.slideType())
                    .layout(toJson(slideContent.layout()))
                    .visualElements(toJson(slideContent.visualElements()))
                    .imageUrl(imageUrl)
                    .imagePrompt(imagePrompt)
                    .build();

            slide.setPresentation(presentation);
            presentation.getSlides().add(slide);
        }
        return presentationRepository.save(presentation);
        }
    private int resolveSlideCount(Integer requested) {
        if (requested == null) return 5;
        return Math.max(1, Math.min(requested, 20)); // cap to avoid runaway image-gen cost
    }

    private String buildSlideBody(SlideContent slideContent) {
        StringBuilder sb = new StringBuilder(
                slideContent.content() != null ? slideContent.content() : ""
        );
        if (slideContent.bullets() != null && !slideContent.bullets().isEmpty()) {
            sb.append("\n");
            for (String bullet : slideContent.bullets()) {
                sb.append("\n- ").append(bullet);
            }
        }
        return sb.toString();
    }
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
    private PresentationContent generateStructuredContent(String source,
                                                          int slideCount,
                                                          String theme,
                                                          String template) {
        String rawJson = aiService.generatePresentationJson(  source,
                slideCount,
                theme,
                template);
        String cleaned = stripJsonFences(rawJson);
        try {
            return objectMapper.readValue(cleaned, PresentationContent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI presentation response: " + e.getMessage(), e);
        }
    }

    private String stripJsonFences(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        return trimmed;
    }

    public List<GeneratedPresentation> getPresentations(String email) {
        validateEmail(email);
        return presentationRepository.findByUserEmail(email);
    }

    public GeneratedPresentation getPresentation(UUID id) {
        return presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentation not found"));
    }

    public String exportPresentation(UUID id) {
        GeneratedPresentation presentation = getPresentation(id);
        StringBuilder builder = new StringBuilder();
        builder.append(presentation.getTitle()).append("\n\n");
        for (PresentationSlide slide : presentation.getSlides()) {
            builder.append("Slide ").append(slide.getSlideOrder()).append(": ")
                    .append(slide.getTitle()).append("\n")
                    .append(slide.getContent()).append("\n\n");
        }
        return builder.toString();
    }

    public GeneratedReport createReport(ReportRequest request) {
        if (request == null) {
            throw new RuntimeException("Report request is required");
        }
        validateEmail(request.getUserEmail());

        User user = findUser(request.getUserEmail());
        Note note = findNote(request.getNoteId());
        String source = resolveSource(request.getSourceContent(), note);

        GeneratedReport report = new GeneratedReport();
        report.setTitle(defaultValue(request.getTitle(), "Generated Report"));
        report.setUserEmail(request.getUserEmail());
        report.setUser(user);
        report.setSourceNote(note);
        report.setSourceContent(source);
        report.setReportType(defaultValue(request.getReportType(), "SUMMARY"));
        if ("SUMMARY".equalsIgnoreCase(report.getReportType())) {
            report.setContent(aiService.summarize(source));
        } else {
            report.setContent(aiService.generateReport(source));
        }
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    public List<GeneratedReport> getReports(String email) {
        validateEmail(email);
        return reportRepository.findByUserEmail(email);
    }

    public GeneratedReport getReport(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    private List<PresentationSlide> buildSlides(String source, String theme) {
        List<String> parts = splitSource(source);
        List<PresentationSlide> slides = new ArrayList<>();

        slides.add(PresentationSlide.builder()
                .title("Overview")
                .content(parts.get(0))
                .slideOrder(1)
                .design(theme)
                .build());
        slides.add(PresentationSlide.builder()
                .title("Key Points")
                .content(parts.size() > 1 ? parts.get(1) : parts.get(0))
                .slideOrder(2)
                .design(theme)
                .build());
        slides.add(PresentationSlide.builder()
                .title("Review")
                .content("Use this slide to discuss takeaways, questions, and next study actions.")
                .slideOrder(3)
                .design(theme)
                .build());

        return slides;
    }

    private String buildReport(String source) {
        return "Summary\n" + source + "\n\nKey Takeaways\n- Review the main ideas.\n- Connect this content with your notes.\n- Create follow-up quiz questions.";
    }

    private List<String> splitSource(String source) {
        String normalized = source == null || source.isBlank() ? "No content provided." : source.trim();
        List<String> parts = new ArrayList<>();
        int midpoint = Math.min(normalized.length(), Math.max(1, normalized.length() / 2));
        parts.add(normalized.substring(0, midpoint));
        parts.add(normalized.substring(midpoint));
        return parts;
    }

    private String resolveSource(String sourceContent, Note note) {
        if (sourceContent != null && !sourceContent.isBlank()) {
            return sourceContent;
        }
        if (note != null) {
            return note.getContent();
        }
        return "";
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Note findNote(UUID noteId) {
        if (noteId == null) {
            return null;
        }
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
