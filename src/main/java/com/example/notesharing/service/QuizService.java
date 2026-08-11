package com.example.notesharing.service;

import com.example.notesharing.DTO.QuizResultDTO;
import com.example.notesharing.DTO.Request.QuizAnswerRequest;
import com.example.notesharing.Repository.QuizRepository;
import com.example.notesharing.modal.Quiz;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Quiz createQuizFromNote(String noteText, String email) {
        List<Map<String, Object>> questions = buildQuestions(noteText);
        List<String> answerKey = Collections.nCopies(questions.size(), "A");

        Quiz quiz = new Quiz();
        quiz.setUserEmail(email);
        quiz.setSourceText(noteText);
        quiz.setQuestionsJson(writeJson(questions));
        quiz.setAnswerKeyJson(writeJson(answerKey));
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());

        return quizRepository.save(quiz);
    }

    public Quiz getQuiz(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public QuizResultDTO playQuiz(UUID quizId, List<QuizAnswerRequest> answers) {
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

        return QuizResultDTO.builder()
                .score(score)
                .total(answerKey.size())
                .build();
    }

    private List<Map<String, Object>> buildQuestions(String noteText) {
        String source = noteText == null || noteText.isBlank()
                ? "your note"
                : noteText.replaceAll("\\s+", " ").trim();

        String preview = source.length() > 120 ? source.substring(0, 120) : source;
        List<String> topics = extractTopics(source);

        List<Map<String, Object>> questions = new ArrayList<>();
        questions.add(question(
                "What is the main topic of this note?",
                List.of(preview, "A calendar reminder", "A payment receipt", "A login credential")
        ));
        questions.add(question(
                "Which option is most useful for reviewing this material?",
                List.of("Summarize the key points", "Ignore the note", "Delete the source", "Change the email")
        ));
        questions.add(question(
                "Which keyword appears important in the note?",
                List.of(topics.get(0), topics.get(1), topics.get(2), topics.get(3))
        ));

        return questions;
    }

    private Map<String, Object> question(String question, List<String> options) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("question", question);
        item.put("options", options);
        return item;
    }

    private List<String> extractTopics(String source) {
        List<String> words = Arrays.stream(source.split("[^A-Za-z0-9]+"))
                .filter(word -> word.length() > 3)
                .distinct()
                .limit(4)
                .toList();

        List<String> topics = new ArrayList<>(words);
        while (topics.size() < 4) {
            topics.add("Study note");
        }
        return topics;
    }

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
