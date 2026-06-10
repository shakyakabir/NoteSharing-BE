package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinShareRequest {
    private String shareCode;
    private String userEmail;
}
