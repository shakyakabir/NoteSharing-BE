package com.example.notesharing.Controller;

import com.example.notesharing.DTO.PageResponse;
import com.example.notesharing.DTO.Request.CreditAdjustRequest;
import com.example.notesharing.DTO.Request.UserPlanRequest;
import com.example.notesharing.DTO.Request.UserStatusRequest;
import com.example.notesharing.DTO.UserAdminDTO;
import com.example.notesharing.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Admin user management. Search/filter is server-side (the page's existing filter state drives the
 * query args). Status/credit/plan mutations delegate through {@code AdminService} to
 * {@code AiCreditService}, keeping the never-negative + history-preserving guarantees in one place.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public PageResponse<UserAdminDTO> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminService.listUsers(search, accountType, status, page, size);
    }

    @PutMapping("/{id}/status")
    public UserAdminDTO setStatus(@PathVariable UUID id, @RequestBody UserStatusRequest request) {
        return adminService.setUserStatus(id, request.getStatus());
    }

    @PostMapping("/{id}/credits")
    public UserAdminDTO adjustCredits(@PathVariable UUID id, @RequestBody CreditAdjustRequest request) {
        return adminService.adjustUserCredits(id, request.getAmount(), request.getReason());
    }

    @PutMapping("/{id}/plan")
    public UserAdminDTO changePlan(@PathVariable UUID id, @RequestBody UserPlanRequest request) {
        return adminService.changeUserPlan(id, request.getPlanConfigId());
    }
}
