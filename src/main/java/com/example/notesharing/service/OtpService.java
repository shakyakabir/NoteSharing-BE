package com.example.notesharing.service;

import com.example.notesharing.Repository.OtpRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.OTP;
import com.example.notesharing.modal.User;
import com.example.notesharing.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OtpService {

    @Autowired
    OtpRepository otpRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    JwtService  jwtService;
//
//
//    public ApiResponse<?>verifyOtp (String email, String otp){
//
//        OTP otpObj=otpRepo.findByEmail(email).orElse(null);
//
//
//
//        if(otpObj==null){
//            return  ApiResponse.builder()
//                    .status("404")
//                    .message("Otp not found")
//                    .build();
//
//        }
//        if(otpObj.getExpiresAt().isBefore(Instant.now())){
//            otpRepo.deleteByEmail(email);
//            return ApiResponse.builder()
//                    .status("404")
//                    .message("Otp Expired")
//                    .build();
//
//
//        }
//
//        if(!otpObj.getOtp().equals(otp)){
//            return ApiResponse.builder()
//                    .status("400")
//                    .message("Invalid Otp")
//                    .build();
//        }
//
//        User user = User.builder()
//                .email(email)
//                .emailVerified(true)
//                .build();
//        userRepo.save(user);
//
//        String accessToken=jwtService.generateToken(email);
//
//        otpRepo.deleteByEmail(email);
//
//        return ApiResponse.builder()
//                .status("200")
//                .message("User Verified and crwated")
//                .data(accessToken)
//                .build();
//
//    }
//}

@Transactional
    public ApiResponse<?> verifyOtp(String email, String otp) {

        OTP otpObj = otpRepo.findTopByEmailOrderByIdDesc(email);

        if (otpObj == null) {
            return ApiResponse.builder()
                    .status("404")
                    .message("Otp not found")
                    .build();
        }

        if (otpObj.getExpiresAt().isBefore(Instant.now())) {

            otpRepo.deleteByEmail(email);

            return ApiResponse.builder()
                    .status("404")
                    .message("Otp Expired")
                    .build();
        }

        if (!otpObj.getOtp().equals(otp)) {
            return ApiResponse.builder()
                    .status("400")
                    .message("Invalid Otp")
                    .build();
        }

        User user = userRepo.findByEmail(email).orElse(null);

        if (user != null) {
            user.setEmailVerified(true);
            userRepo.save(user);
        }

        String accessToken = jwtService.generateToken(email);

        otpRepo.deleteByEmail(email);

        return ApiResponse.builder()
                .status("200")
                .message("User Verified")
                .data(accessToken)
                .build();
    }

}