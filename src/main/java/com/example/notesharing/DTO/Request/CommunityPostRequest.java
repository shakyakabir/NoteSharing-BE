package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostRequest {

    private String authorEmail;
    private String title;
    private String tag;
    private String content;
}
