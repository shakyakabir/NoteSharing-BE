package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Admin/public view of an admin-configurable subscription plan. Carries raw values (price as a
 * number, features as a structured list, the enforcement {@code tier}, allowance + refresh cadence);
 * the frontend derives display strings ({@code "$"+price}, current-plan flag, button label).
 */
@Getter
@Setter
@Builder
public class SubscriptionPlanDTO {

    private String id;
    private String name;
    private double price;
    private String period;
    private String tier;
    private int creditAllowance;
    private int refreshDays;
    private List<PlanFeatureDTO> features;
    private boolean active;
    private int sortOrder;
}
