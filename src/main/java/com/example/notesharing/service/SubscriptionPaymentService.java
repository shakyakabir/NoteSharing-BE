package com.example.notesharing.service;


import com.example.notesharing.DTO.Request.SubscriptionCheckoutRequest;
import com.example.notesharing.DTO.Response.payment.PaymentInitiationResponse;
import com.example.notesharing.Enum.PaymentMethod;
import com.example.notesharing.Enum.PaymentStatus;
import com.example.notesharing.Enum.SubscriptionTier;
import com.example.notesharing.Repository.SubscriptionPaymentRepository;
import com.example.notesharing.Repository.SubscriptionPlanConfigRepository;
import com.example.notesharing.Repository.UserRepository;
import com.example.notesharing.modal.SubscriptionPayment;
import com.example.notesharing.modal.SubscriptionPlanConfig;

import com.example.notesharing.modal.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionPaymentService {

    @Autowired
    private SubscriptionPlanConfigRepository planRepository;

    @Autowired
    private SubscriptionPaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiCreditService aiCreditService;


    // =========================================================
    // ESEWA CONFIGURATION
    // =========================================================

    @Value("${esewa.product-code}")
    private String esewaProductCode;

    @Value("${esewa.secret-key}")
    private String esewaSecretKey;

    @Value("${esewa.payment-url}")
    private String esewaPaymentUrl;

    @Value("${esewa.success-url}")
    private String esewaSuccessUrl;

    @Value("${esewa.failure-url}")
    private String esewaFailureUrl;


    // =========================================================
    // KHALTI CONFIGURATION
    // =========================================================

    @Value("${khalti.secret-key}")
    private String khaltiSecretKey;

    @Value("${khalti.base-url}")
    private String khaltiBaseUrl;

    @Value("${khalti.website-url}")
    private String khaltiWebsiteUrl;

    @Value("${khalti.return-url}")
    private String khaltiReturnUrl;
    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // CREATE CHECKOUT
    // =========================================================

    public PaymentInitiationResponse createCheckout(
            SubscriptionCheckoutRequest request
    ) {

        // -----------------------------------------------------
        // Validate request
        // -----------------------------------------------------

        if (request == null) {
            throw new RuntimeException(
                    "Checkout request is required"
            );
        }

        if (request.getEmail() == null ||
                request.getEmail().isBlank()) {

            throw new RuntimeException(
                    "User email is required"
            );
        }

        if (request.getPlanId() == null) {

            throw new RuntimeException(
                    "Plan ID is required"
            );
        }

        if (request.getPaymentMethod() == null) {

            throw new RuntimeException(
                    "Payment method is required"
            );
        }


        // -----------------------------------------------------
        // Validate user
        // -----------------------------------------------------

        userRepository.findByEmail(
                        request.getEmail()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );


        // -----------------------------------------------------
        // Get plan
        // -----------------------------------------------------

        SubscriptionPlanConfig plan =
                planRepository.findById(
                                request.getPlanId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Plan not found"
                                )
                        );


        // -----------------------------------------------------
        // Check active plan
        // -----------------------------------------------------

        if (!plan.isActive()) {

            throw new RuntimeException(
                    "This subscription plan is not active"
            );
        }


        // -----------------------------------------------------
        // Get price FROM DATABASE
        // Never trust frontend price
        // -----------------------------------------------------

        BigDecimal amount =
                BigDecimal.valueOf(plan.getPrice());


        if (amount == null ||
                amount.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new RuntimeException(
                    "Invalid plan price"
            );
        }


        // -----------------------------------------------------
        // Create our internal transaction UUID
        // -----------------------------------------------------

        String transactionUuid =
                UUID.randomUUID().toString();


        // -----------------------------------------------------
        // Create pending payment
        // -----------------------------------------------------

        SubscriptionPayment payment =
                SubscriptionPayment.builder()
                        .userEmail(
                                request.getEmail()
                        )
                        .plan(plan)
                        .amount(amount)
                        .paymentMethod(
                                request.getPaymentMethod()
                        )
                        .status(
                                PaymentStatus.PENDING
                        )
                        .transactionUuid(
                                transactionUuid
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();


        payment =
                paymentRepository.save(payment);


        // -----------------------------------------------------
        // Send to selected payment gateway
        // -----------------------------------------------------

        return switch (
                request.getPaymentMethod()
                ) {

            case ESEWA -> createEsewaCheckout(
                    payment,
                    plan
            );

            case KHALTI -> createKhaltiCheckout(
                    payment,
                    plan
            );
        };
    }


    // =========================================================
    // ESEWA CHECKOUT
    // =========================================================

    private PaymentInitiationResponse createEsewaCheckout(
            SubscriptionPayment payment,
            SubscriptionPlanConfig plan
    ) {

        String transactionUuid =
                payment.getTransactionUuid();


        // -----------------------------------------------------
        // Amount
        // -----------------------------------------------------

        String amount =
                payment.getAmount()
                        .stripTrailingZeros()
                        .toPlainString();


        // -----------------------------------------------------
        // Fields that are signed
        // -----------------------------------------------------

        String signedFieldNames =
                "total_amount,transaction_uuid,product_code";


        // -----------------------------------------------------
        // Message used for HMAC SHA256
        // -----------------------------------------------------

        String message =
                "total_amount=" + amount +
                        ",transaction_uuid=" + transactionUuid +
                        ",product_code=" + esewaProductCode;


        // -----------------------------------------------------
        // Generate signature
        // -----------------------------------------------------

        String signature =
                generateEsewaSignature(
                        message,
                        esewaSecretKey
                );


        // -----------------------------------------------------
        // Create eSewa form data
        // -----------------------------------------------------

        Map<String, String> formData =
                new LinkedHashMap<>();


        formData.put(
                "amount",
                amount
        );

        formData.put(
                "tax_amount",
                "0"
        );

        formData.put(
                "total_amount",
                amount
        );

        formData.put(
                "transaction_uuid",
                transactionUuid
        );

        formData.put(
                "product_code",
                esewaProductCode
        );

        formData.put(
                "product_service_charge",
                "0"
        );

        formData.put(
                "product_delivery_charge",
                "0"
        );

        formData.put(
                "success_url",
                esewaSuccessUrl
        );

        formData.put(
                "failure_url",
                esewaFailureUrl
        );

        formData.put(
                "signed_field_names",
                signedFieldNames
        );

        formData.put(
                "signature",
                signature
        );


        // -----------------------------------------------------
        // Return checkout information
        // -----------------------------------------------------

        return PaymentInitiationResponse.builder()
                .paymentId(
                        payment.getId().toString()
                )
                .paymentMethod(
                        PaymentMethod.ESEWA.name()
                )
                .paymentUrl(
                        esewaPaymentUrl
                )
                .transactionUuid(
                        transactionUuid
                )
                .formData(
                        formData
                )
                .build();
    }

    private void activateSubscription(
            SubscriptionPayment payment
    ) {

        User user = userRepository.findByEmail(
                payment.getUserEmail()
        ).orElseThrow(() ->
                new RuntimeException(
                        "User not found: " + payment.getUserEmail()
                )
        );

        SubscriptionPlanConfig plan = payment.getPlan();

        Instant start = Instant.now();

        Instant end = start.plusSeconds(
                plan.getRefreshDays() * 24L * 60L * 60L
        );

        // ============================================
        // SUBSCRIPTION
        // ============================================

        user.setSubscriptionTier(
                SubscriptionTier.valueOf(
                        plan.getTier().name()
                )
        );

        user.setSubscriptionStartAt(start);

        user.setSubscriptionEndAt(end);

        // ============================================
        // AI QUOTA
        // ============================================

        user.setAiQuotaUsed(0);

        user.setAiQuotaLimit(
                plan.getCreditAllowance()
        );

        user.setAiQuotaExpires(end);

        // ============================================
        // SAVE
        // ============================================

        userRepository.save(user);

        // ============================================
        // ACTIVATE AI SUBSCRIPTION (Model A)
        // ============================================
        // The profile fields above only drive /api/user/profile. The AI credit + premium-feature
        // gate reads the AiSubscription record, so mirror the paid plan onto it (resets credits to
        // the plan allowance, sets the premium window, logs a GRANT). Without this a paying user
        // stays FREE for the gate and premium-only tools remain locked.
        aiCreditService.changeUserPlan(payment.getUserEmail(), plan.getId());
    }
    public void handleEsewaSuccess(String encodedData) {

        try {

            // ============================================
            // 1. Decode Base64 response from eSewa
            // ============================================

            byte[] decodedBytes =
                    Base64.getDecoder().decode(encodedData);

            String json =
                    new String(
                            decodedBytes,
                            StandardCharsets.UTF_8
                    );

            System.out.println("Decoded eSewa response:");
            System.out.println(json);


            // ============================================
            // 2. Convert JSON to Map
            // ============================================

            Map<String, Object> response =
                    objectMapper.readValue(
                            json,
                            Map.class
                    );


            String status =
                    String.valueOf(
                            response.get("status")
                    );

            String transactionUuid =
                    String.valueOf(
                            response.get("transaction_uuid")
                    );

            String productCode =
                    String.valueOf(
                            response.get("product_code")
                    );

            String totalAmount =
                    String.valueOf(
                            response.get("total_amount")
                    );

            String returnedSignature =
                    String.valueOf(
                            response.get("signature")
                    );


            // ============================================
            // 3. Basic validation
            // ============================================

            if (!"COMPLETE".equalsIgnoreCase(status)) {

                throw new RuntimeException(
                        "eSewa payment is not complete. Status: "
                                + status
                );
            }

            if (!esewaProductCode.equals(productCode)) {

                throw new RuntimeException(
                        "Invalid eSewa product code"
                );
            }


            // ============================================
            // 4. Find our internal payment
            // ============================================

            SubscriptionPayment payment =
                    paymentRepository
                            .findByTransactionUuid(
                                    transactionUuid
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Payment not found for transaction: "
                                                    + transactionUuid
                                    )
                            );


            // ============================================
            // 5. Verify amount
            // ============================================

            BigDecimal expectedAmount =
                    payment.getAmount();

            BigDecimal receivedAmount =
                    new BigDecimal(totalAmount);

            if (expectedAmount.compareTo(receivedAmount) != 0) {

                throw new RuntimeException(
                        "Payment amount mismatch"
                );
            }


            // ============================================
            // 6. Verify eSewa response signature
            // ============================================

            String signedFieldNames =
                    String.valueOf(
                            response.get("signed_field_names")
                    );

            String[] fields =
                    signedFieldNames.split(",");

            StringBuilder message =
                    new StringBuilder();

            for (int i = 0; i < fields.length; i++) {

                String field =
                        fields[i].trim();

                Object value =
                        response.get(field);

                if (i > 0) {
                    message.append(",");
                }

                message.append(field)
                        .append("=")
                        .append(value);
            }


            String generatedSignature =
                    generateEsewaSignature(
                            message.toString(),
                            esewaSecretKey
                    );


            if (!generatedSignature.equals(returnedSignature)) {

                throw new RuntimeException(
                        "Invalid eSewa response signature"
                );
            }


            // ============================================
            // 7. Prevent duplicate processing
            // ============================================

            if (payment.getStatus() ==
                    PaymentStatus.COMPLETED) {

                System.out.println(
                        "Payment already processed: "
                                + transactionUuid
                );

                return;
            }


            // ============================================
            // 8. Mark payment successful
            // ============================================

            payment.setStatus(
                    PaymentStatus.COMPLETED
            );

            payment.setCompletedAt(
                    LocalDateTime.now()
            );

            payment.setProviderTransactionId(
                    String.valueOf(
                            response.get("transaction_code")
                    )
            );

            paymentRepository.save(payment);


            // ============================================
            // 9. UPDATE USER SUBSCRIPTION
            // ============================================

            activateSubscription(payment);


            System.out.println(
                    "eSewa payment successfully processed: "
                            + transactionUuid
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process eSewa success response",
                    e
            );
        }
    }

    // =========================================================
    // ESEWA SIGNATURE
    // =========================================================

    private String generateEsewaSignature(
            String message,
            String secret
    ) {

        try {

            Mac mac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );


            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            secret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );


            mac.init(secretKey);


            byte[] hash =
                    mac.doFinal(
                            message.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate eSewa signature",
                    e
            );
        }
    }


    // =========================================================
    // KHALTI CHECKOUT
    // =========================================================

    private PaymentInitiationResponse createKhaltiCheckout(
            SubscriptionPayment payment,
            SubscriptionPlanConfig plan
    ) {

        // -----------------------------------------------------
        // Khalti initiate endpoint
        // -----------------------------------------------------

        String endpoint =
                khaltiBaseUrl +
                        "/epayment/initiate/";


        // -----------------------------------------------------
        // Convert rupees to paisa
        //
        // Example:
        // Rs. 500 = 50000 paisa
        // -----------------------------------------------------

        long amountInPaisa =
                payment.getAmount()
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .longValueExact();


        // -----------------------------------------------------
        // Request body
        // -----------------------------------------------------

        Map<String, Object> body =
                new LinkedHashMap<>();


        body.put(
                "return_url",
                khaltiReturnUrl +
                        "?paymentId=" +
                        payment.getId()
        );


        body.put(
                "website_url",
                khaltiWebsiteUrl
        );


        body.put(
                "amount",
                amountInPaisa
        );


        body.put(
                "purchase_order_id",
                payment.getTransactionUuid()
        );


        body.put(
                "purchase_order_name",
                plan.getName()
        );


        // -----------------------------------------------------
        // Customer information
        // -----------------------------------------------------

        Map<String, Object> customerInfo =
                new LinkedHashMap<>();


        customerInfo.put(
                "email",
                payment.getUserEmail()
        );


        body.put(
                "customer_info",
                customerInfo
        );


        // -----------------------------------------------------
        // Call Khalti
        // -----------------------------------------------------

        RestClient restClient =
                RestClient.builder()
                        .baseUrl(khaltiBaseUrl)
                        .build();


        Map<String, Object> response =
                restClient.post()
                        .uri("/epayment/initiate/")
                        .header(
                                "Authorization",
                                "Key " + khaltiSecretKey
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(body)
                        .retrieve()
                        .body(Map.class);


        // -----------------------------------------------------
        // Validate response
        // -----------------------------------------------------

        if (response == null) {

            throw new RuntimeException(
                    "Empty response received from Khalti"
            );
        }


        Object pidxObject =
                response.get("pidx");


        Object paymentUrlObject =
                response.get("payment_url");


        if (pidxObject == null ||
                paymentUrlObject == null) {

            throw new RuntimeException(
                    "Invalid response received from Khalti: "
                            + response
            );
        }


        String pidx =
                pidxObject.toString();


        String paymentUrl =
                paymentUrlObject.toString();


        // -----------------------------------------------------
        // Save Khalti pidx
        // -----------------------------------------------------

        payment.setPidx(pidx);

        paymentRepository.save(payment);


        // -----------------------------------------------------
        // Return checkout response
        // -----------------------------------------------------

        return PaymentInitiationResponse.builder()
                .paymentId(
                        payment.getId().toString()
                )
                .paymentMethod(
                        PaymentMethod.KHALTI.name()
                )
                .paymentUrl(
                        paymentUrl
                )
                .transactionUuid(
                        payment.getTransactionUuid()
                )
                .pidx(
                        pidx
                )
                .build();
    }
}