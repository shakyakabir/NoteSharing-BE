package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.AdvertisementRequest;
import com.example.notesharing.modal.Advertisement;
import com.example.notesharing.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin Ads CRUD. Returns {@link Advertisement} entities directly (raw, like the other admin
 * controllers); the frontend formats the numeric fields and computes the per-ad revenue for display.
 * {@code list} returns every ad regardless of status (the user-facing feed shows ACTIVE only).
 */
@RestController
@RequestMapping("/api/admin/ads")
public class AdminAdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    @GetMapping
    public List<Advertisement> list() {
        return advertisementService.getAllAds();
    }

    @PostMapping
    public Advertisement create(@RequestBody AdvertisementRequest request) {
        return advertisementService.createAd(request);
    }

    @PutMapping("/{id}")
    public Advertisement update(@PathVariable UUID id, @RequestBody AdvertisementRequest request) {
        return advertisementService.updateAd(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        advertisementService.deleteAd(id);
    }
}
