package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReportRequest {

    private String title;
    private String userEmail;
    private UUID noteId;
    private String sourceContent;
    private String reportType;
}
