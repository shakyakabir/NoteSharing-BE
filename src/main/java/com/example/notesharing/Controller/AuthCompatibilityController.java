package com.example.notesharing.Controller;

import com.example.notesharing.DTO.RegisterRequest;
import com.example.notesharing.payload.ApiResponse;
import com.example.notesharing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthCompatibilityController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
