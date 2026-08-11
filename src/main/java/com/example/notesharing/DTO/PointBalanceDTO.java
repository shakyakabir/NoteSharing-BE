package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PointBalanceDTO {

    private String email;
    private int pointBalance;
    private int streakDays;
    private int aiQuotaUsed;
}
