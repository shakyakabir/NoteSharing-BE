package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Response.UserProfileResponse;
import com.example.notesharing.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(
            UserProfileService userProfileService
    ) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            Authentication authentication
    ) {

        String email = authentication.getName();

        UserProfileResponse profile =
                userProfileService.getProfile(email);

        return ResponseEntity.ok(profile);
    }
}
