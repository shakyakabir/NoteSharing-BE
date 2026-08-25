package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

/**
 * Admin user status change ({@code PUT /api/admin/users/{id}/status}). {@code status} is "Active" or
 * "Suspended" (matching the frontend), mapped to {@code User.isActive}.
 */
@Getter
@Setter
public class UserStatusRequest {

    private String status;
}
