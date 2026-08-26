package com.example.notesharing.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {



    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String summarize(String text) {

        String prompt = """
                You are an expert study assistant.

                Summarize the following note into concise study notes.

                Keep:
                - 1 short summary paragraph
                - 5 bullet points

                Note:
                %s
                """.formatted(text);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public String generateReport(
            String sourceText,
            String userPrompt,
            Integer detailLevel,
            String writingStyle,
            String referenceContent
    ) {
        if (sourceText == null || sourceText.isBlank()) {
            throw new IllegalArgumentException("Cannot generate a report without source content");
        }

        String detailInstruction = mapDetailLevel(detailLevel);
        String styleInstruction = mapWritingStyle(writingStyle);

        String userInstructionBlock = (userPrompt != null && !userPrompt.isBlank())
                ? """
                  USER GOAL:
                  The user has specifically asked for the following. Prioritize this over the
                  default structure below where they conflict (e.g. if they ask for an executive
                  summary rather than a full report, follow that instead):
                  %s
                  """.formatted(userPrompt)
                : "";

        String referenceBlock = (referenceContent != null && !referenceContent.isBlank())
                ? """
                  REFERENCE REPORT TO MIMIC:
                  Study the structure, section ordering, tone, and formatting conventions of the
                  reference report below. Reuse that structure and style for the new report.
                  Do NOT copy its factual content, only its style and structure.
 
                  --- REFERENCE START ---
                  %s
                  --- REFERENCE END ---
                  """.formatted(referenceContent)
                : "";

        String prompt = """
                You are an expert report-writing assistant.
 
                Create a professional report based on the source material below.
 
                %s
                %s
 
                Detail level: %s
                Writing style: %s
 
                Default structure (use unless the user's goal above calls for something else):
                - Title
                - Introduction
                - Main Discussion
                - Conclusion
 
                SOURCE MATERIAL:
                %s
                """.formatted(
                userInstructionBlock,
                referenceBlock,
                detailInstruction,
                styleInstruction,
                sourceText
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private String mapDetailLevel(Integer level) {
        if (level == null) {
            return "Balanced — moderate depth, cover all key points without excessive elaboration.";
        }
        return switch (level) {
            case 1 -> "Brief — concise, only the most essential points, minimal elaboration.";
            case 3 -> "Detailed — comprehensive and thorough, with explanations, examples, and nuance.";
            default -> "Balanced — moderate depth, cover all key points without excessive elaboration.";
        };
    }

    private String mapWritingStyle(String style) {
        if (style == null || style.isBlank()) {
            return "Professional / Academic — formal, clear, objective tone.";
        }
        return switch (style) {
            case "Casual / Conversational" ->
                    "Casual / Conversational — friendly, approachable, plain language.";
            case "Technical / Concise" ->
                    "Technical / Concise — precise terminology, minimal fluff, dense with information.";
            default -> "Professional / Academic — formal, clear, objective tone.";
        };
    }


    public String extractKeyPoints(String text) {

        String prompt = """
                Extract the key points from the following note.

                Return only bullet points.

                %s
                """.formatted(text);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
    public String generateQuizQuestions(String sourceText, String difficulty, int questionCount) {
        if (sourceText == null || sourceText.isBlank()) {
            throw new IllegalArgumentException("Cannot generate a quiz without source content");
        }

        String difficultyGuidance = switch (difficulty == null ? "" : difficulty.toUpperCase()) {
            case "BEGINNER" ->
                    "Simple recall questions. Straightforward wording. Distractors should be clearly wrong to anyone who read the note.";
            case "ADVANCED" ->
                    "Questions that require connecting two or more ideas from the note, not just single-fact recall.";
            case "EXPERT" ->
                    "Challenging application/synthesis questions. Distractors must be highly plausible and require precise understanding to rule out.";
            default ->
                    "Moderate difficulty. Some light interpretation required, not pure copy-paste recall.";
        };

        String prompt = """
                You are an expert quiz writer.
 
                Create exactly %d multiple-choice questions based ONLY on the note below.
                Do not invent facts that aren't in the note.
 
                Difficulty: %s
                Guidance: %s
 
                Rules:
                - Each question has exactly 4 options.
                - Exactly one option is correct.
                - "correctIndex" is the 0-based index of the correct option.
                - Never hint at or reveal the correct answer inside the question text.
                - Vary which index holds the correct answer across questions.
                - Return ONLY valid JSON, with no markdown code fences and no commentary,
                  matching exactly this schema:
 
                [
                  {
                    "question": "string",
                    "options": ["string", "string", "string", "string"],
                    "correctIndex": 0
                  }
                ]
 
                NOTE:
                %s
                """.formatted(questionCount, difficulty, difficultyGuidance, sourceText);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
    public String generatePresentationJson(
            String sourceContent,
            int slideCount,
            String visualTheme,
            String theme,
            String template
    ) {

        String prompt = """
    You are an expert presentation designer and information architect.

    Design a professional presentation based on the provided source.

    Your responsibility is NOT only to write the content.
    You must also determine the best visual structure for every slide.

    Presentation requirements:

    Number of slides: %d
    Theme: %s
    Visual Theme: %s
    Template: %s

    IMPORTANT DESIGN RULES:

    1. Do not use the same layout for every slide.
    2. Choose the most appropriate slide type based on the content.
    3. Use images only when they improve understanding.
    4. Do NOT add an image to every slide.
    5. Determine the best position of text, images and visual elements.
    6. Use visual storytelling.
    7. Use diagrams when the information benefits from a diagram.
    8. Use circular layouts for cycles/processes.
    9. Use timelines for chronological information.
    10. Use comparison layouts when comparing concepts.
    11. Use statistic layouts when numerical information is important.
    12. Use cards when multiple independent concepts need to be presented.
    13. Use quote layouts for important statements.
    14. Avoid overcrowding.
    15. Keep each slide visually balanced.
    16. Do not repeat the same visual structure unnecessarily.

    CONTENT RULES (CRITICAL — DO NOT SKIP):

    17. The "content" field must NEVER be null or empty, on ANY slide, regardless of
        slideType. This applies even to timeline, cards, circle, comparison, and
        statistics slides that also use visualElements or bullets — those elements
        SUPPLEMENT content, they do not replace it.
    18. "content" must be substantive: 2 to 4 full sentences of real explanatory text,
        not a single fragment. A single short sentence is NOT enough.
    19. If a slide's main information lives in bullets or visualElements, "content"
        must still contain a short framing paragraph that introduces or contextualizes
        that data (e.g. for a timeline slide, content explains why the timeline matters,
        not just restate the dates).

    THEME RULES — the Theme value controls tone and vocabulary, not just visuals.
    Theme will be one of: academic, professional, creative.

    - academic: precise, formal, scholarly tone. Use domain-accurate terminology,
      cite mechanisms/causes where relevant, avoid casual phrasing or hype. Write as
      if for a university lecture or research briefing.
    - professional: concise, business-appropriate, outcome- and impact-focused tone.
      Favor clarity and actionable framing over academic depth. Write as if for a
      corporate stakeholder briefing.
    - creative: vivid, story-driven, engaging tone. Use descriptive language, analogy,
      and narrative framing while staying accurate. Write as if for an audience that
      wants to be captivated, not just informed.

    If the Theme value does not exactly match one of the above, infer the closest tone
    and apply it consistently across every slide's content and bullets.

    AVAILABLE SLIDE TYPES:

    - title
    - section
    - content
    - split
    - image_text
    - full_image
    - cards
    - comparison
    - timeline
    - process
    - circle
    - statistics
    - quote
    - diagram
    - conclusion

    Return ONLY valid JSON.

    Schema:

    {
      "title": "string",
      "slides": [
        {
          "title": "string",
          "subtitle": "string or null",
          "content": "string — REQUIRED, 2-4 sentences, never null or empty",

          "bullets": [],

          "slideType": "one of the supported slide types",

          "layout": {
            "type": "string",
            "alignment": "string",
            "contentPosition": "string",
            "imagePosition": "string"
          },

          "image": {
            "required": true,
            "prompt": "string",
            "position": "string",
            "width": 0.45,
            "height": 0.70
          },

          "visualElements": []
        }
      ]
    }

    IMAGE RULES:

    If an image is not useful:

    "image": {
      "required": false,
      "prompt": null,
      "position": null,
      "width": 0,
      "height": 0
    }

    If an image is useful:

    "image": {
      "required": true,
      "prompt": "specific visual description",
      "position": "left/right/top/bottom/background",
      "width": 0.45,
      "height": 0.70
    }

    VISUAL ELEMENT RULES:

    For circular information use:

    {
      "type": "circle",
      "position": "center",
      "data": {
        "centerText": "string",
        "items": ["string"]
      }
    }

    For timelines use:

    {
      "type": "timeline",
      "position": "center",
      "data": {
        "items": [
          {
            "year": "string",
            "text": "string"
          }
        ]
      }
    }

    For comparisons use:

    {
      "type": "comparison",
      "position": "center",
      "data": {
        "columns": [
          {
            "title": "string",
            "items": ["string"]
          }
        ]
      }
    }

    For cards use:

    {
      "type": "cards",
      "position": "center",
      "data": {
        "cards": [
          { "title": "string", "text": "string" }
        ]
      }
    }

    Generate exactly %d slides.

    SOURCE:

    %s
    """.formatted(
                slideCount,
                theme,
                visualTheme,
                template,
                slideCount,
                sourceContent
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
