package com.example.notesharing.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One line item in a plan's feature list ({@code {text, included}}), matching the frontend pricing
 * card shape. Used both in responses and inside {@code PlanRequest}; stored as JSON in
 * {@code SubscriptionPlanConfig.features}. No-args + all-args ctors let Jackson (de)serialize it.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanFeatureDTO {

    private String text;
    private boolean included;
}
