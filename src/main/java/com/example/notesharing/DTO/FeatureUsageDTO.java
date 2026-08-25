package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * One AI feature's real usage share, computed from the CONSUME ledger. Feeds the dashboard
 * "AI Feature Usage" bars and the analytics donut. {@code feature} is the enum key, {@code name} the
 * display name, {@code count} the number of consumes, {@code percent} its share of all consumes.
 */
@Getter
@Setter
@Builder
public class FeatureUsageDTO {

    private String feature;
    private String name;
    private long count;
    private double percent;
}
