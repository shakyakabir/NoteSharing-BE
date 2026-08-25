package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Admin user plan change ({@code PUT /api/admin/users/{id}/plan}). {@code planConfigId} references a
 * {@link com.example.notesharing.modal.SubscriptionPlanConfig}; the service resets the user's balance
 * to that plan's allowance and mirrors its tier onto the enforcement field.
 */
@Getter
@Setter
public class UserPlanRequest {

    private UUID planConfigId;
}
