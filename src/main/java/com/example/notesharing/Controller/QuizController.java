package com.example.notesharing.Controller;

import com.example.notesharing.DTO.QuizResultDTO;
import com.example.notesharing.DTO.Request.QuizAnswerRequest;
import com.example.notesharing.DTO.Request.QuizCreateRequest;
import com.example.notesharing.modal.Quiz;
import com.example.notesharing.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QuizController {
    @Autowired
    private QuizService quizService;

    /**
     * Creates a quiz from either pasted note text OR an uploaded file (PDF/TXT) - send exactly
     * one of `noteText` / `file`. `mode` = solo | collaborative, `difficulty` = beginner |
     * intermediate | advanced | expert, matching the frontend's QuizSetupForm.
     */
    @PostMapping(value = "/quiz/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Quiz createQuiz(
            @RequestPart("data") QuizCreateRequest data,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return quizService.createQuiz(
                data.getNoteText(),
                file,
                data.getEmail(),
                data.getDifficulty(),
                data.getMode(),
                data.getNotebookId()
        );
    }

    @GetMapping("/quiz/{id}")
    public Quiz getQuiz(@PathVariable UUID id) {
        return quizService.getQuiz(id);
    }

    /**
     * `playerEmail` is whoever is actually taking the quiz right now - in solo mode that's the
     * quiz owner, in collaborative mode it may be any friend in the session. Points (if any)
     * are credited to playerEmail, not quiz.userEmail.
     */
    @PostMapping("/play")
    public QuizResultDTO playQuiz(
            @RequestParam UUID quizId,
            @RequestParam String playerEmail,
            @RequestBody List<QuizAnswerRequest> answers
    ) {
        return quizService.playQuiz(quizId, playerEmail, answers);
    }
}

//    @Autowired
//    private QuizService quizService;
//
//    @PostMapping("/from-note")
//    public Quiz createFromNote(@RequestBody(required = false) String noteText,
//                               @RequestParam String email) {
//        return quizService.createQuizFromNote(noteText, email);
//    }
//
//    @GetMapping("/quiz/{id}")
//    public Quiz getQuiz(@PathVariable UUID id) {
//        return quizService.getQuiz(id);
//    }
//
//    @PostMapping("/play")
//    public QuizResultDTO playQuiz(@RequestParam UUID quizId,
//                                  @RequestBody List<QuizAnswerRequest> answers) {
//        return quizService.playQuiz(quizId, answers);
//    }
//}
