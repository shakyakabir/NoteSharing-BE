package com.example.notesharing.service;

import com.example.notesharing.DTO.AuthResponseDTO;
import com.example.notesharing.DTO.LoginDTO;
import com.example.notesharing.DTO.RegisterRequest;
import com.example.notesharing.Repository.OtpRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.OTP;
import com.example.notesharing.modal.User;
import com.example.notesharing.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
                .password(registerRequest.getPassword())
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
    public ApiResponse<?> LoginService(LoginDTO loginRequest){


        User user=userRepo.findByEmail(loginRequest.getEmail()).orElse(null);
        if(user == null){
            return  ApiResponse.builder()
                    .status("404")
                    .message("User not found")
                    .build();



        }
        if(!user.isEmailVerified()){
            return ApiResponse.builder()
                    .status("403")
                    .message("Email not Verify")
                    .build();

        }

        if(!user.getPassword().equals(loginRequest.getPassword())){
            return ApiResponse.builder()
                    .status("400")
                    .message("Invalid credential")
                    .build();
        }
        if (!user.getEmail().equals(loginRequest.getEmail())) {
            return
                    ApiResponse.builder()
                            .status("400")
                            .message("Invalid credential")
                            .build();
        }


        String accessToken=jwtService.generateToken(loginRequest.getEmail());
        String refreshToken=jwtService.generateRefreshToken(loginRequest.getEmail());

        return ApiResponse.builder()
                .status("200")
                .message("login success")
                .data(new AuthResponseDTO(accessToken,refreshToken))
                .build();

    }
}
