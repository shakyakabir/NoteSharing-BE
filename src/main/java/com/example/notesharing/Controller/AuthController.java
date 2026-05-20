package com.example.notesharing.Controller;

import com.example.notesharing.DTO.LoginDTO;
import com.example.notesharing.DTO.RegisterRequest;
import com.example.notesharing.payload.ApiResponse;
import com.example.notesharing.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

   private final AuthService authService;

   @PostMapping("/register")
   public ApiResponse<?> register(@RequestBody RegisterRequest request) {
       return authService.register(request);

   }

@PostMapping("/login")
    public ApiResponse<?> login (@RequestBody LoginDTO login){
       return authService.LoginService(login);
}



}
