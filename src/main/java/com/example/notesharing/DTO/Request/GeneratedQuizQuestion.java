package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class GeneratedQuizQuestion {
    private String question;
    private List<String> options;
    private int correctIndex;
}
