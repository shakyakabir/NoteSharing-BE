package com.example.notesharing.Controller;

import com.example.notesharing.DTO.CreditStatusDTO;
import com.example.notesharing.DTO.SubscriptionDTO;
import com.example.notesharing.DTO.SubscriptionPlanDTO;
import com.example.notesharing.modal.CreditTransaction;
import com.example.notesharing.service.AiCreditService;
import com.example.notesharing.service.SubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI credit + subscription endpoints. Identity always comes from the JWT session (SecurityContext)
 * - there is no email/plan/cost parameter a client could tamper with, and no client-callable
 * "consume" endpoint (credits are only ever spent server-side inside the AI generation flows).
 */
@RestController
@RequestMapping("/api")
public class AiCreditController {

    @Autowired
    private AiCreditService aiCreditService;

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/ai/credits")
    public CreditStatusDTO getCredits() {
        return aiCreditService.status();
    }

    @GetMapping("/ai/usage")
    public List<CreditTransaction> getUsage() {
        return aiCreditService.usage();
    }

    @GetMapping("/ai/feature-costs")
    public Map<String, Integer> getFeatureCosts() {
        return aiCreditService.featureCosts();
    }

    @GetMapping("/subscription")
    public SubscriptionDTO getSubscription() {
        return aiCreditService.subscription();
    }

    /** Active admin-configured plans for the user-facing pricing surface (auth required, any user). */
    @GetMapping("/subscription/plans")
    public List<SubscriptionPlanDTO> getPlans() {
        return subscriptionPlanService.listActive();
    }

    @PostMapping("/subscription/upgrade")
    public SubscriptionDTO upgrade() {
        return aiCreditService.upgradeToPremiumWithPoints();
    }
}
