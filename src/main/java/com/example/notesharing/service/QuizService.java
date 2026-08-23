package com.example.notesharing.service;


import com.example.notesharing.DTO.QuizResultDTO;
import com.example.notesharing.DTO.Request.GeneratedQuizQuestion;
import com.example.notesharing.DTO.Request.QuizAnswerRequest;
import com.example.notesharing.Enum.Difficulty;
import com.example.notesharing.Enum.QuizMode;
import com.example.notesharing.Repository.QuizRepository;
import com.example.notesharing.modal.Quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizService {

    private static final int QUESTIONS_PER_QUIZ = 5;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private UserScoreService userScoreService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a quiz from either raw note text OR an uploaded file (PDF/TXT) - exactly one
     * of noteText / file should be provided by the caller.
     */
    public Quiz createQuiz(
            String noteText,
            MultipartFile file,
            String email,
            String difficultyStr,
            String modeStr,
            String notebookId
    ) throws IOException {
        String sourceText = resolveSourceText(noteText, file);
        if (sourceText == null || sourceText.isBlank()) {
            throw new IllegalArgumentException("Provide note text or upload a file (PDF/TXT) to generate a quiz");
        }

        Difficulty difficulty = Difficulty.fromString(difficultyStr);
        QuizMode mode = QuizMode.fromString(modeStr);

        // NOTE: this MUST be GeneratedQuizQuestion (question/options/correctIndex from the AI),
        // never QuizAnswerRequest (questionIndex/answer, submitted later by the player).
        List<GeneratedQuizQuestion> generated = generateQuestions(sourceText, difficulty);

        List<Map<String, Object>> questionsForStorage = new ArrayList<>();
        List<String> answerKey = new ArrayList<>();

        for (GeneratedQuizQuestion q : generated) {
            ShuffledOptions shuffled = shuffleOptions(q.getOptions(), q.getCorrectIndex());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("question", q.getQuestion());
            item.put("options", shuffled.options());
            questionsForStorage.add(item);

            answerKey.add(indexToLetter(shuffled.correctIndex()));
        }

        Quiz quiz = new Quiz();
        quiz.setUserEmail(email);
        quiz.setSourceText(sourceText);
        quiz.setQuestionsJson(writeJson(questionsForStorage));
        quiz.setAnswerKeyJson(writeJson(answerKey));
        quiz.setDifficulty(difficulty);
        quiz.setMode(mode);
        quiz.setPointsPerCompletion(difficulty.getPoints());
        quiz.setNotebookId(notebookId);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());

        return quizRepository.save(quiz);
    }

    public Quiz getQuiz(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    /**
     * Scores the attempt and, ONLY for SOLO quizzes, credits points to the player.
     * Collaborative quizzes (playing with friends) never award points, no matter who plays them.
     */
    public QuizResultDTO playQuiz(UUID quizId, String playerEmail, List<QuizAnswerRequest> answers) {
        Quiz quiz = getQuiz(quizId);
        List<String> answerKey = readAnswerKey(quiz.getAnswerKeyJson());

        int score = 0;
        if (answers == null) {
            answers = List.of();
        }

        for (QuizAnswerRequest answer : answers) {
            if (answer == null) {
                continue;
            }

            int index = answer.getQuestionIndex();
            if (index >= 0 && index < answerKey.size()
                    && answerKey.get(index).equalsIgnoreCase(answer.getAnswer())) {
                score++;
            }
        }

        int total = answerKey.size();
        long pointsEarned = 0;

        boolean isSolo = quiz.getMode() == QuizMode.SOLO;
        if (isSolo && total > 0 && playerEmail != null && !playerEmail.isBlank()) {
            double ratio = (double) score / total;
            pointsEarned = Math.round(quiz.getPointsPerCompletion() * ratio);
            userScoreService.addPoints(playerEmail, pointsEarned);
        }
        // COLLABORATIVE mode: intentionally never calls userScoreService, regardless of score.

        return QuizResultDTO.builder()
                .score(score)
                .total(total)
                .pointsEarned(pointsEarned)
                .mode(quiz.getMode().name())
                .build();
    }

    // --- SOURCE RESOLUTION ---

    private String resolveSourceText(String noteText, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

            if (filename.endsWith(".pdf")) {
                return extractPdfText(file);
            } else if (filename.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            } else if (filename.endsWith(".docx")) {
                // TODO: wire up Apache POI (XWPFDocument) here if you need DOCX upload support.
                throw new UnsupportedOperationException(
                        "DOCX upload isn't wired up yet - please upload a PDF or TXT, or paste the note text.");
            } else {
                throw new IllegalArgumentException("Unsupported file type. Please upload a PDF or TXT file.");
            }
        }
        return noteText;
    }

    /**
     * PDFBox 3.x removed the static PDDocument.load(...) methods - use org.apache.pdfbox.Loader
     * instead. (If you're actually on PDFBox 2.x, swap this back to
     * PDDocument.load(file.getInputStream()) and drop the Loader import.)
     */
    private String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // --- AI GENERATION ---

    private List<GeneratedQuizQuestion> generateQuestions(String sourceText, Difficulty difficulty) {
        String rawJson = aiService.generateQuizQuestions(sourceText, difficulty.name(), QUESTIONS_PER_QUIZ);
        List<GeneratedQuizQuestion> generated = parseGeneratedQuestions(rawJson);

        if (generated.isEmpty()) {
            throw new RuntimeException("AI did not return any usable quiz questions - please try again");
        }

        for (GeneratedQuizQuestion q : generated) {
            if (q.getOptions() == null || q.getOptions().size() != 4) {
                throw new RuntimeException("AI returned a question without exactly 4 options");
            }
            if (q.getCorrectIndex() < 0 || q.getCorrectIndex() > 3) {
                throw new RuntimeException("AI returned an out-of-range correctIndex");
            }
        }

        return generated;
    }

    private List<GeneratedQuizQuestion> parseGeneratedQuestions(String rawJson) {
        String cleaned = rawJson == null ? "" : rawJson
                .replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, new TypeReference<List<GeneratedQuizQuestion>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse AI-generated quiz questions", e);
        }
    }

    // --- OPTION SHUFFLING (so the correct answer isn't always the same position) ---

    private record ShuffledOptions(List<String> options, int correctIndex) {}

    private ShuffledOptions shuffleOptions(List<String> options, int correctIndex) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            order.add(i);
        }
        Collections.shuffle(order);

        List<String> shuffled = new ArrayList<>();
        int newCorrectIndex = 0;
        for (int newPos = 0; newPos < order.size(); newPos++) {
            int originalPos = order.get(newPos);
            shuffled.add(options.get(originalPos));
            if (originalPos == correctIndex) {
                newCorrectIndex = newPos;
            }
        }

        return new ShuffledOptions(shuffled, newCorrectIndex);
    }

    private String indexToLetter(int index) {
        return String.valueOf((char) ('A' + index));
    }

    // --- JSON HELPERS ---

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to create quiz", e);
        }
    }

    private List<String> readAnswerKey(String answerKeyJson) {
        try {
            return objectMapper.readValue(
                    answerKeyJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to read quiz answers", e);
        }
    }
}