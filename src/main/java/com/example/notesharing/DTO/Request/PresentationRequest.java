package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PresentationRequest {

    private String title;
    private String userEmail;
    private UUID noteId;
    private String sourceContent;
    private Integer slideCount;
    private String theme;
    private String templateName;
    private Boolean includeImages;
}
