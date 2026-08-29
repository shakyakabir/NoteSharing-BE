package com.example.notesharing.Controller;

import com.example.notesharing.modal.Advertisement;
import com.example.notesharing.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User-facing ads. FREE-tier users get the active ads; premium users get an empty list (ad-free).
 * Impression/click tracking feeds the CPM + CPC revenue shown in admin analytics.
 */
@RestController
@RequestMapping("/api")
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @GetMapping("/ads/active")
    public List<Advertisement> getActiveAds() {
        return advertisementService.getActiveAdsFor(currentEmail());
    }

    @PostMapping("/ads/{id}/impression")
    public void recordImpression(@PathVariable UUID id) {
        advertisementService.recordImpression(id);
    }

    @PostMapping("/ads/{id}/click")
    public void recordClick(@PathVariable UUID id) {
        advertisementService.recordClick(id);
    }

    /** Authenticated user's email, or null when anonymous (treated as non-premium -> ads shown). */
    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return auth.getName();
    }
}
