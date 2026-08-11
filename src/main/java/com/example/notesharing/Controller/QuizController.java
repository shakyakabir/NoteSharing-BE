package com.example.notesharing.Controller;

import com.example.notesharing.DTO.QuizResultDTO;
import com.example.notesharing.DTO.Request.QuizAnswerRequest;
import com.example.notesharing.modal.Quiz;
import com.example.notesharing.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping("/from-note")
    public Quiz createFromNote(@RequestBody(required = false) String noteText,
                               @RequestParam String email) {
        return quizService.createQuizFromNote(noteText, email);
    }

    @GetMapping("/quiz/{id}")
    public Quiz getQuiz(@PathVariable UUID id) {
        return quizService.getQuiz(id);
    }

    @PostMapping("/play")
    public QuizResultDTO playQuiz(@RequestParam UUID quizId,
                                  @RequestBody List<QuizAnswerRequest> answers) {
        return quizService.playQuiz(quizId, answers);
    }
}
