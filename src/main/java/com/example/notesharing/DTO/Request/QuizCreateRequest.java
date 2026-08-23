package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizCreateRequest {
    private String noteText;
    private String email;
    private String difficulty;
    private String mode;
    private String notebookId;
}