package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

/**
 * Admin credit adjustment for a user ({@code POST /api/admin/users/{id}/credits}). {@code amount} may
 * be negative to deduct; the resulting balance is clamped to {@code >= 0} (never negative) and an
 * ADJUST transaction is logged with {@code reason}.
 */
@Getter
@Setter
public class CreditAdjustRequest {

    private int amount;
    private String reason;
}
