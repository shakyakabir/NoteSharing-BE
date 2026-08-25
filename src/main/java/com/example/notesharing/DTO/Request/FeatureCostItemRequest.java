package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

/**
 * One row of the AI-credit-costs bulk save ({@code PUT /api/admin/ai-credit-costs} accepts a list of
 * these). {@code feature} is the enum key; the other fields are applied only when present so the
 * page can send its edited draft. Cost is clamped to {@code >= 0} server-side.
 */
@Getter
@Setter
public class FeatureCostItemRequest {

    private String feature;
    private Integer cost;
    private Boolean premiumOnly;
    private String status;
}
