package com.example.notesharing.service;

import com.example.notesharing.DTO.Request.AdvertisementRequest;
import com.example.notesharing.Enum.SubscriptionPlan;
import com.example.notesharing.Repository.AdvertisementRepository;
import com.example.notesharing.Repository.AiSubscriptionRepository;
import com.example.notesharing.modal.Advertisement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Ads domain, mirroring {@link RewardService}. Earnings use CPM + CPC:
 * {@code impressions/1000 * cpmRate + clicks * cpcRate}, accumulated from the tracked counters and
 * surfaced in admin analytics via {@link #totalAdRevenue()}. Only FREE-tier users are served ads.
 */
@Service
public class AdvertisementService {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private AiSubscriptionRepository aiSubscriptionRepository;

    /** Admin: every ad regardless of status (self-service {@link #getActiveAdsFor} shows ACTIVE only). */
    public List<Advertisement> getAllAds() {
        return advertisementRepository.findAll();
    }

    /**
     * Active ads for a viewer. Premium users are ad-free, so they always get an empty list;
     * FREE-tier (and unknown/anonymous) users get every ACTIVE ad.
     */
    public List<Advertisement> getActiveAdsFor(String email) {
        if (isPremium(email)) {
            return List.of();
        }
        return advertisementRepository.findByActiveTrue();
    }

    public Advertisement createAd(AdvertisementRequest request) {
        if (request == null) {
            throw new RuntimeException("Advertisement request is required");
        }
        Advertisement ad = new Advertisement();
        ad.setTitle(required(request.getTitle(), "Advertisement title is required"));
        ad.setDescription(request.getDescription());
        ad.setImageUrl(request.getImageUrl());
        ad.setTargetUrl(request.getTargetUrl());
        ad.setPlacement(normalizePlacement(request.getPlacement()));
        ad.setCpmRate(Math.max(0, request.getCpmRate()));
        ad.setCpcRate(Math.max(0, request.getCpcRate()));
        String status = normalizeStatus(request.getStatus());
        ad.setStatus(status);
        ad.setActive("ACTIVE".equals(status));
        ad.setCreatedAt(LocalDateTime.now());
        return advertisementRepository.save(ad);
    }

    /** Admin: edit an existing ad. Keeps {@code active} in sync with {@code status}. */
    public Advertisement updateAd(UUID id, AdvertisementRequest request) {
        if (request == null) {
            throw new RuntimeException("Advertisement request is required");
        }
        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            ad.setTitle(request.getTitle());
        }
        ad.setDescription(request.getDescription());
        ad.setImageUrl(request.getImageUrl());
        ad.setTargetUrl(request.getTargetUrl());
        ad.setPlacement(normalizePlacement(request.getPlacement()));
        ad.setCpmRate(Math.max(0, request.getCpmRate()));
        ad.setCpcRate(Math.max(0, request.getCpcRate()));
        String status = normalizeStatus(request.getStatus());
        ad.setStatus(status);
        ad.setActive("ACTIVE".equals(status));
        return advertisementRepository.save(ad);
    }

    /** Admin: hard-delete an ad. */
    public void deleteAd(UUID id) {
        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Advertisement not found"));
        advertisementRepository.delete(ad);
    }

    /** Track one impression (ad shown). No-op if the ad no longer exists. */
    public void recordImpression(UUID id) {
        advertisementRepository.findById(id).ifPresent(ad -> {
            ad.setImpressions(ad.getImpressions() + 1);
            advertisementRepository.save(ad);
        });
    }

    /** Track one click. No-op if the ad no longer exists. */
    public void recordClick(UUID id) {
        advertisementRepository.findById(id).ifPresent(ad -> {
            ad.setClicks(ad.getClicks() + 1);
            advertisementRepository.save(ad);
        });
    }

    /**
     * Total ad revenue across all ads (CPM + CPC): {@code impressions/1000 * cpmRate + clicks * cpcRate}.
     * Feeds the admin analytics revenue metrics.
     */
    public double totalAdRevenue() {
        return advertisementRepository.findAll().stream()
                .mapToDouble(this::adRevenue)
                .sum();
    }

    /** Revenue for a single ad (CPM + CPC). */
    public double adRevenue(Advertisement ad) {
        return (ad.getImpressions() / 1000.0) * ad.getCpmRate() + ad.getClicks() * ad.getCpcRate();
    }

    private boolean isPremium(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return aiSubscriptionRepository.findByUserEmail(email)
                .map(sub -> sub.getPlan() == SubscriptionPlan.PREMIUM)
                .orElse(false);
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value;
    }

    /** Normalize an ad status to one of ACTIVE | PAUSED | DRAFT, defaulting to ACTIVE. */
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "ACTIVE", "PAUSED", "DRAFT" -> normalized;
            default -> "ACTIVE";
        };
    }

    /** Normalize a placement to one of BANNER | SIDEBAR | INLINE, defaulting to BANNER. */
    private String normalizePlacement(String placement) {
        if (placement == null || placement.isBlank()) {
            return "BANNER";
        }
        String normalized = placement.trim().toUpperCase();
        return switch (normalized) {
            case "BANNER", "SIDEBAR", "INLINE" -> normalized;
            default -> "BANNER";
        };
    }
}
