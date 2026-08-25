package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin AI-credit-costs row. Mirrors the frontend {@code FeatureItem} data fields ({@code feature}
 * is the stable enum key the page maps to its local icon/colour; {@code cost} is the current cost).
 * Icons/Tailwind classes stay client-side - only serialisable data is sent.
 */
@Getter
@Setter
@Builder
public class AiFeatureConfigDTO {

    private String id;
    private String feature;
    private String name;
    private String description;
    private String status;
    private int cost;
    private boolean premiumOnly;
}
