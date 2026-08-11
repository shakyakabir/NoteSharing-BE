package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerRequest {

    private int questionIndex;
    private String answer;
}
