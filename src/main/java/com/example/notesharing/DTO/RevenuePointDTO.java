package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * A point on the analytics revenue series (one per month). {@code subscription} is the completed
 * eSewa payment total for that month; {@code ads} is the accumulated CPM+CPC ad revenue (see
 * {@code AdminService.revenueBreakdown} for why ads sits in the current-month bucket).
 */
@Getter
@Setter
@Builder
public class RevenuePointDTO {

    private String month;
    private double subscription;
    private double ads;
}
