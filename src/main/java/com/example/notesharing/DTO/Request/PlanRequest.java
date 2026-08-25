package com.example.notesharing.DTO.Request;

import com.example.notesharing.DTO.PlanFeatureDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Admin create/update payload for a {@link com.example.notesharing.modal.SubscriptionPlanConfig}.
 * All fields are optional (wrapper types) so an update applies only the fields that are present;
 * create fills sensible defaults (see {@code SubscriptionPlanService}). The admin "Create Plan" modal
 * only sends {@code name} + {@code price}; the rest are derived.
 */
@Getter
@Setter
public class PlanRequest {

    private String name;
    private Double price;
    private String period;
    private String tier;
    private Integer creditAllowance;
    private Integer refreshDays;
    private List<PlanFeatureDTO> features;
    private Boolean active;
    private Integer sortOrder;
}
