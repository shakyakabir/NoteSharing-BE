//package com.example.notesharing.Controller;
//
//import com.example.notesharing.DTO.LoginDTO;
//import com.example.notesharing.DTO.RegisterRequest;
//import com.example.notesharing.payload.ApiResponse;
//import com.example.notesharing.service.AuthService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//
//public class AuthController {
//
//   private final AuthService authService;
//
//   @PostMapping("/register")
//   public ApiResponse<?> register(@RequestBody RegisterRequest request) {
//       return authService.register(request);
//
//   }
//
//@PostMapping("/login")
//    public ApiResponse<?> login (@RequestBody LoginDTO login){
//       return authService.LoginService(login);
//}
//
//    @GetMapping("/user")
//    public Object user(@AuthenticationPrincipal org.springframework.security.oauth2.core.user.OAuth2User user) {
//        return user.getAttributes();
//    }
//
//}



package com.example.notesharing.Controller;

import com.example.notesharing.DTO.LoginDTO;
import com.example.notesharing.DTO.RegisterRequest;
import com.example.notesharing.DTO.Request.ForgotPasswordRequest;
import com.example.notesharing.DTO.Request.ResetPasswordRequest;
import com.example.notesharing.payload.ApiResponse;
import com.example.notesharing.service.AuthService;
import com.example.notesharing.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginDTO login, HttpServletResponse response) {
        return authService.LoginService(login,response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
@Autowired
    JwtService jwtService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        String email = jwtService.extractEmail(refreshToken);

        if (!jwtService.isRefreshTokenValid(refreshToken, email)) {
            return ResponseEntity.status(401).build();
        }

        String newAccessToken = jwtService.generateToken(email);

        Cookie accessCookie = new Cookie("accessToken", newAccessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60);

        response.addCookie(accessCookie);

        return ResponseEntity.ok().build();
    }
}