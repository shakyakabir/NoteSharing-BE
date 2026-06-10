package com.example.notesharing.Controller;

import com.example.notesharing.modal.OTP;
import com.example.notesharing.payload.ApiResponse;
import com.example.notesharing.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")

public class OTPController {
@Autowired
private OtpService otpService;

@PostMapping("/verify-otp")
    public ApiResponse<?> verifyOtp(@RequestBody OTP otp){

    return otpService.verifyOtp(otp.getEmail(), otp.getOtp());


}

}
