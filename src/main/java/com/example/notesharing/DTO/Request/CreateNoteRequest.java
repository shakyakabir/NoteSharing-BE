package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNoteRequest {

    private String title;
    private String content;
    private String visibility;
}
