package com.example.notesharing.service;

import com.example.notesharing.DTO.AuthResponseDTO;
import com.example.notesharing.DTO.LoginDTO;
import com.example.notesharing.DTO.RegisterRequest;
import com.example.notesharing.Repository.OtpRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.OTP;
import com.example.notesharing.modal.User;
import com.example.notesharing.payload.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    @Autowired
private   UserRepository userRepo;


@Autowired
  private  OtpRepository otpRepo;

@Autowired
private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    public ApiResponse<?> register(RegisterRequest registerRequest) {


        if (userRepo.existsByEmail(registerRequest.getEmail())) {
            return ApiResponse.builder()
                    .status("404")
                    .message("Email already exists")
                    .data(null)
                    .build();
        }
        String otp=String.valueOf((int)(Math.random()*900000)+100000);

        OTP otpObj=OTP.builder()
                .email(registerRequest.getEmail())
                .otp(otp)
                .expiresAt(Instant.now().plusSeconds(180))
                .build();

        otpRepo.save(otpObj);

        emailService.sendOtp(registerRequest.getEmail(),otp);



        User user=User.builder()
                .userName(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .emailVerified(false)
                .build();

        userRepo.save(user);

        return ApiResponse.builder()
                .status("200")
                .message("User Register Successfully")
                .data("otp send")
                .build();
    }



    @Autowired
    JwtService jwtService;
    public ApiResponse<?> LoginService(LoginDTO loginRequest, HttpServletResponse response) {

        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (user == null) {
            return ApiResponse.builder()
                    .status("404")
                    .message("User not found")
                    .build();
        }

        if (!user.isEmailVerified()) {
            return ApiResponse.builder()
                    .status("403")
                    .message("Email not verified")
                    .build();
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ApiResponse.builder()
                    .status("400")
                    .message("Invalid credentials")
                    .build();
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 15); // 15 min

        // 🔥 REFRESH TOKEN COOKIE (optional but recommended)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days

        // ADD COOKIES TO RESPONSE
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ApiResponse.builder()
                .status("200")
                .message("login success")
                .data(user.getEmail())
                .build();
    }
}
