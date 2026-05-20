package com.example.notesharing.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
}
