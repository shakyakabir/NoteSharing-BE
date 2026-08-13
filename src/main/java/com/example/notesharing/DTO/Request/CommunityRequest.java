package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityRequest {

    private String name;
    private String category;
    private String description;
    private String ownerEmail;
}
