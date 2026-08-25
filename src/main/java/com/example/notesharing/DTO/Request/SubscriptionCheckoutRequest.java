package com.example.notesharing.DTO.Request;
import com.example.notesharing.Enum.PaymentMethod;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionCheckoutRequest {

    private UUID planId;

    private String email;

    private PaymentMethod paymentMethod;
}
