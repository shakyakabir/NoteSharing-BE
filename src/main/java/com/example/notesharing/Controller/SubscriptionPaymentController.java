package com.example.notesharing.Controller;


import com.example.notesharing.DTO.Request.SubscriptionCheckoutRequest;
import com.example.notesharing.DTO.Response.payment.PaymentInitiationResponse;
import com.example.notesharing.service.SubscriptionPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SubscriptionPaymentController {
    @Autowired
    private SubscriptionPaymentService subscriptionPaymentService;


    /**
     * Create a payment checkout for a subscription plan.
     *
     * POST:
     * /api/subscriptions/payment/checkout
     *
     * Request:
     * {
     *     "planId": "plan-uuid",
     *     "email": "user@gmail.com",
     *     "paymentMethod": "ESEWA"
     * }
     */
    @PostMapping("/checkout")
    public ResponseEntity<PaymentInitiationResponse> createCheckout(
            @RequestBody SubscriptionCheckoutRequest request
    ) {

        PaymentInitiationResponse response =
                subscriptionPaymentService.createCheckout(request);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/subscriptions/payment/esewa/success")
    public ResponseEntity<String> esewaSuccess(
            @RequestParam("data") String data
    ) {
        try {
            subscriptionPaymentService.handleEsewaSuccess(data);

            return ResponseEntity.ok(
                    "eSewa payment successful. Subscription activated."
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.badRequest().body(
                    "Payment verification failed: " + e.getMessage()
            );
        }
    }
    @GetMapping("/subscriptions/payment/esewa/failure")
    public ResponseEntity<String> esewaFailure(
            @RequestParam(value = "data", required = false) String data
    ) {
        System.out.println("eSewa payment failed");
        System.out.println("Data: " + data);

        return ResponseEntity.ok(
                "eSewa payment failed"
        );
    }
}
