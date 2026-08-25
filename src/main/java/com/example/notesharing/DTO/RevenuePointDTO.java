package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A point on the analytics revenue/churn series. Superset of the frontend chart keys so either chart
 * can bind to it. Always returned as an empty series (no payment system) - honest, not fabricated.
 */
@Getter
@Setter
@Builder
public class RevenuePointDTO {

    private String month;
    private double mrr;
    private double churn;
    private double subscription;
    private double ads;
}
