package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardItemRequest {

    private String title;
    private String description;
    private int cost;
    private String rewardType;
    private int aiCost;
    private int maxUses;
    private String status;
}
