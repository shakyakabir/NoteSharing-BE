package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizAnswerRequest {

    private int questionIndex;
    private String answer;
}
