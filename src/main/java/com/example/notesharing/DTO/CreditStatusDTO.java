package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Read-only view of a user's AI credit balance + refresh schedule. All values are computed
 * server-side (after any due refresh) - the frontend only displays them.
 */
@Getter
@Setter
@Builder
public class CreditStatusDTO {

    private String plan;
    private String status;
    private int currentCredits;
    private int maxCredits;
    private int refreshDays;
    private LocalDateTime nextRefresh;
    private long daysUntilRefresh;
}
