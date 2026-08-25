package com.example.notesharing.DTO.Response.payment;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PaymentInitiationResponse {

    private String paymentId;

    private String paymentMethod;

    private String paymentUrl;

    private String transactionUuid;

    private String pidx;

    private Map<String, String> formData;
    }