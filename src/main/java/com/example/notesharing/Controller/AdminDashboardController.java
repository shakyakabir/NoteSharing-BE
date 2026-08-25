package com.example.notesharing.Controller;

import com.example.notesharing.DTO.AdminAnalyticsDTO;
import com.example.notesharing.DTO.AdminDashboardDTO;
import com.example.notesharing.DTO.AdminMeDTO;
import com.example.notesharing.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin dashboard + analytics + identity. Gated to ADMIN by the {@code /api/admin/**} security rule,
 * so {@code GET /me} doubles as the frontend's admin gate (200 for admins, 403 otherwise). Returns
 * raw DTOs, consistent with {@code AiCreditController}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public AdminDashboardDTO dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/analytics")
    public AdminAnalyticsDTO analytics(@RequestParam(required = false) String range) {
        return adminService.analytics(range);
    }

    @GetMapping("/me")
    public AdminMeDTO me() {
        return adminService.me();
    }
}
